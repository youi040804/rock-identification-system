package com.itgu.Mapper;

import com.itgu.Pojo.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByIsDeletedAndInTrainingSet(int i, int i1);
}
