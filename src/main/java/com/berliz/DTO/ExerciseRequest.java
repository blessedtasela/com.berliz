package com.berliz.DTO;

import com.berliz.models.Exercise;
import com.berliz.models.MuscleGroup;
import com.berliz.models.SubTask;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Data
public class ExerciseRequest {
    private String name;
    private String description;
    private Set<MuscleGroup> muscleGroups;
    private Set<Exercise> exercises;
    private Set<SubTask> subTasks;
    private MultipartFile video;
}
