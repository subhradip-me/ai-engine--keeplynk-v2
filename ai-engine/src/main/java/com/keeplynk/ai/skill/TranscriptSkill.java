package com.keeplynk.ai.skill;

import com.keeplynk.ai.agent.AgentContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Order(0) // Execute before all other skills to provide transcript content
public class TranscriptSkill implements Skill {

    private static final int MAX_VIDEO_INFO_CHARS = 4000;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Patterns for different video platforms
    private static final Pattern YOUTUBE_PATTERN = Pattern.compile(
        "(youtube\\.com/watch\\?v=|youtu\\.be/)([a-zA-Z0-9_-]+)"
    );
    private static final Pattern VIMEO_PATTERN = Pattern.compile(
        "vimeo\\.com/(\\d+)"
    );

    @Override
    public void apply(AgentContext context) {
        String url = context.getUrl();
        
        if (url == null) {
            context.addReasoning("TranscriptSkill: No URL provided, skipping");
            return;
        }

        String videoId = extractVideoId(url);
        if (videoId == null) {
            context.addReasoning("TranscriptSkill: Not a supported video URL, skipping");
            return;
        }

        context.addReasoning("TranscriptSkill: Video URL detected - " + getVideoPlatform(url));
        
        try {
            String videoInfo = getVideoTranscript(url, videoId);
            
            if (videoInfo != null && !videoInfo.isEmpty()) {
                String trimmedVideoInfo = truncate(videoInfo, MAX_VIDEO_INFO_CHARS);
                // Store transcript/video info in context for other skills to use
                context.getMemory().put("videoTranscript", trimmedVideoInfo);
                context.getMemory().put("isVideoContent", true);
                
                // Update content with video information
                String enhancedContent = context.getContent() != null ? 
                    context.getContent() + "\n\n[Video Transcript]\n" + trimmedVideoInfo : 
                    "[Video Transcript]\n" + trimmedVideoInfo;
                context.setContent(enhancedContent);
                
                context.addReasoning("TranscriptSkill: Successfully extracted video transcript/metadata");
            } else {
                context.addReasoning("TranscriptSkill: Could not extract video transcript - falling back to webpage analysis");
                // Fallback to webpage analysis for unsupported platforms
                fallbackToPageAnalysis(context, url);
            }
            
        } catch (Exception e) {
            context.addReasoning("TranscriptSkill: Error extracting video info - " + e.getMessage());
            // Fallback to webpage analysis
            fallbackToPageAnalysis(context, url);
        }
    }

    private String extractVideoId(String url) {
        // YouTube
        Matcher youtubeMatcher = YOUTUBE_PATTERN.matcher(url);
        if (youtubeMatcher.find()) {
            return youtubeMatcher.group(2);
        }
        
        // Vimeo
        Matcher vimeoMatcher = VIMEO_PATTERN.matcher(url);
        if (vimeoMatcher.find()) {
            return vimeoMatcher.group(1);
        }
        
        return null;
    }

    private String getVideoPlatform(String url) {
        if (url.contains("youtube.com") || url.contains("youtu.be")) return "YouTube";
        if (url.contains("vimeo.com")) return "Vimeo";
        return "Unknown";
    }

    private String getVideoTranscript(String url, String videoId) {
        if (url.contains("youtube.com") || url.contains("youtu.be")) {
            return getYouTubeTranscript(videoId);
        } else if (url.contains("vimeo.com")) {
            return getVimeoInfo(videoId);
        }
        return null;
    }

    private String getYouTubeTranscript(String videoId) {
        try {
            // First, try to get video metadata using oEmbed (no API key required)
            String oembedUrl = "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=" + videoId + "&format=json";
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(oembedUrl))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(java.time.Duration.ofSeconds(10))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JsonNode jsonNode = objectMapper.readTree(response.body());
                String title = jsonNode.path("title").asText();
                String author = jsonNode.path("author_name").asText();
                
                // Try to get captions from YouTube's auto-generated captions API
                String transcript = getYouTubeAutoCaption(videoId);
                
                StringBuilder videoInfo = new StringBuilder();
                videoInfo.append("Title: ").append(title).append("\n");
                videoInfo.append("Channel: ").append(author).append("\n");
                
                if (transcript != null && !transcript.isEmpty()) {
                    videoInfo.append("Transcript: ").append(transcript);
                } else {
                    // If no transcript, get detailed description from the page
                    String pageDescription = getYouTubePageDescription(videoId);
                    if (pageDescription != null) {
                        videoInfo.append("Description: ").append(pageDescription);
                    }
                }
                
                return videoInfo.toString();
            }
        } catch (Exception e) {
            // Fall through to return null
        }
        return null;
    }

    private String getYouTubeAutoCaption(String videoId) {
        try {
            // Try to access YouTube's transcript endpoint
            String captionUrl = "https://www.youtube.com/api/timedtext?lang=en&v=" + videoId;
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(captionUrl))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(java.time.Duration.ofSeconds(10))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200 && !response.body().isEmpty()) {
                // Parse XML captions and extract text
                String xmlContent = response.body();
                if (xmlContent.contains("<text")) {
                    return parseXMLCaptions(xmlContent);
                }
            }
        } catch (Exception e) {
            // Ignore errors and return null
        }
        return null;
    }

    private String parseXMLCaptions(String xmlContent) {
        try {
            // Simple XML parsing to extract text content
            StringBuilder transcript = new StringBuilder();
            Pattern textPattern = Pattern.compile("<text[^>]*>([^<]+)</text>");
            Matcher matcher = textPattern.matcher(xmlContent);
            
            while (matcher.find()) {
                String text = matcher.group(1);
                // Decode HTML entities
                text = text.replaceAll("&amp;", "&")
                          .replaceAll("&lt;", "<")
                          .replaceAll("&gt;", ">")
                          .replaceAll("&quot;", "\"")
                          .replaceAll("&#39;", "'");
                transcript.append(text).append(" ");
            }
            
            return transcript.toString().trim();
        } catch (Exception e) {
            return null;
        }
    }

    private String getYouTubePageDescription(String videoId) {
        try {
            String pageUrl = "https://www.youtube.com/watch?v=" + videoId;
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(pageUrl))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(java.time.Duration.ofSeconds(10))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String content = response.body();
                // Extract description from meta tag
                Pattern descPattern = Pattern.compile("<meta name=\"description\" content=\"([^\"]+)\"");
                Matcher matcher = descPattern.matcher(content);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        } catch (Exception e) {
            // Ignore errors
        }
        return null;
    }

    private String getVimeoInfo(String videoId) {
        try {
            // Use Vimeo's oEmbed API
            String oembedUrl = "https://vimeo.com/api/oembed.json?url=https://vimeo.com/" + videoId;
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(oembedUrl))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(java.time.Duration.ofSeconds(10))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JsonNode jsonNode = objectMapper.readTree(response.body());
                String title = jsonNode.path("title").asText();
                String description = jsonNode.path("description").asText();
                String author = jsonNode.path("author_name").asText();
                
                StringBuilder videoInfo = new StringBuilder();
                videoInfo.append("Title: ").append(title).append("\n");
                videoInfo.append("Author: ").append(author).append("\n");
                if (!description.isEmpty()) {
                    videoInfo.append("Description: ").append(description);
                }
                
                return videoInfo.toString();
            }
        } catch (Exception e) {
            // Fall through to return null
        }
        return null;
    }

    private void fallbackToPageAnalysis(AgentContext context, String url) {
        try {
            String pageContent = fetchPageContent(url);
            if (pageContent != null) {
                // Extract basic information and mark it as fallback content
                String fallbackInfo = "Note: Could not access video transcript. Analysis based on page metadata.\n\n";
                
                // Extract title and description from page
                String title = extractFromPage(pageContent, "<title>([^<]+)</title>");
                String description = extractFromPage(pageContent, "<meta name=\"description\" content=\"([^\"]+)\"");
                
                if (title != null) fallbackInfo += "Page Title: " + title + "\n";
                if (description != null) fallbackInfo += "Page Description: " + description + "\n";
                
                context.getMemory().put("videoTranscript", fallbackInfo);
                context.getMemory().put("isVideoContent", true);
                context.getMemory().put("isFallbackContent", true);
                
                String enhancedContent = context.getContent() != null ? 
                    context.getContent() + "\n\n[Video Page Analysis]\n" + fallbackInfo : 
                    "[Video Page Analysis]\n" + fallbackInfo;
                context.setContent(enhancedContent);
                
                context.addReasoning("TranscriptSkill: Using fallback page analysis for video content");
            }
        } catch (Exception e) {
            context.addReasoning("TranscriptSkill: Fallback analysis also failed - " + e.getMessage());
        }
    }

    private String extractFromPage(String content, String regex) {
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    private String fetchPageContent(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(java.time.Duration.ofSeconds(10))
                .build();

            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            }
        } catch (Exception e) {
            // Ignore errors and return null
        }
        return null;
    }

    private static String truncate(String value, int maxChars) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxChars) {
            return value;
        }
        return value.substring(0, maxChars);
    }
}
