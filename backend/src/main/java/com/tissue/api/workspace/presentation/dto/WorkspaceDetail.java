package com.tissue.api.workspace.presentation.dto;

import java.time.LocalDateTime;

import com.tissue.api.workspace.domain.model.Workspace;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class WorkspaceDetail {

	private Long id;
	private String code;
	private String name;
	private String description;
	private int memberCount;
	private Long createdBy;
	private LocalDateTime createdAt;
	private Long updatedBy;
	private LocalDateTime updatedAt;

	@Builder
	public WorkspaceDetail(
		Long id,
		String code,
		String name,
		String description,
		int memberCount,
		Long createdBy,
		LocalDateTime createdAt,
		Long updatedBy,
		LocalDateTime updatedAt
	) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.description = description;
		this.memberCount = memberCount;
		this.createdBy = createdBy;
		this.createdAt = createdAt;
		this.updatedBy = updatedBy;
		this.updatedAt = updatedAt;
	}

	public static WorkspaceDetail from(Workspace workspace) {
		return WorkspaceDetail.builder()
			.id(workspace.getId())
			.code(workspace.getCode())
			.name(workspace.getName())
			.description(workspace.getDescription())
			.memberCount(workspace.getMemberCount())
			.createdBy(workspace.getCreatedBy())
			.createdAt(workspace.getCreatedDate())
			.updatedBy(workspace.getLastModifiedBy())
			.updatedAt(workspace.getLastModifiedDate())
			.build();
	}
}
