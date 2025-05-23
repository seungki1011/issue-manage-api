package com.tissue.api.issue.presentation.controller.dto.request.create;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.tissue.api.issue.domain.model.enums.IssueType;
import com.tissue.api.issue.domain.model.Issue;
import com.tissue.api.workspace.domain.model.Workspace;

@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY,
	property = "type"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = CreateEpicRequest.class, name = "EPIC"),
	@JsonSubTypes.Type(value = CreateStoryRequest.class, name = "STORY"),
	@JsonSubTypes.Type(value = CreateTaskRequest.class, name = "TASK"),
	@JsonSubTypes.Type(value = CreateBugRequest.class, name = "BUG"),
	@JsonSubTypes.Type(value = CreateSubTaskRequest.class, name = "SUB_TASK")
})
public interface CreateIssueRequest {

	CommonIssueCreateFields common();

	IssueType getType();

	Issue toIssue(Workspace workspace);
}
