package com.keeplynk.ai.agent;

import com.keeplynk.ai.skill.DescriptionSkill;
import com.keeplynk.ai.skill.Skill;
import com.keeplynk.ai.skill.TagSkill;
import com.keeplynk.ai.skill.TitleSkill;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ResourceAgent implements Agent {

    private final List<Skill> skills;

    public ResourceAgent(List<Skill> skills) {
        this.skills = skills;
    }

    @Override
    public void execute(AgentContext context) {
        Map<String, Boolean> needs = context.getNeeds();
        
        // If needs is not specified, run all skills (backward compatible)
        if (needs == null || needs.isEmpty()) {
            for (Skill skill : skills) {
                skill.apply(context);
            }
            return;
        }
        
        // Conditional execution based on needs (Auto Organise feature)
        for (Skill skill : skills) {
            boolean shouldExecute = false;
            
            // Check if this skill is needed
            if (skill instanceof TitleSkill && needs.getOrDefault("title", false)) {
                shouldExecute = true;
            } else if (skill instanceof DescriptionSkill && needs.getOrDefault("description", false)) {
                shouldExecute = true;
            } else if (skill instanceof TagSkill && needs.getOrDefault("tags", false)) {
                shouldExecute = true;
            }
            
            if (shouldExecute) {
                context.addReasoning("Executing " + skill.getClass().getSimpleName() + " (requested by needs)");
                skill.apply(context);
            } else {
                context.addReasoning("Skipping " + skill.getClass().getSimpleName() + " (not needed)");
            }
        }
    }
}
