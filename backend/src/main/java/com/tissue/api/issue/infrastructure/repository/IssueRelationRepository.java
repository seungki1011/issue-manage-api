package com.tissue.api.issue.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tissue.api.issue.domain.model.IssueRelation;

public interface IssueRelationRepository extends JpaRepository<IssueRelation, Long> {
}
