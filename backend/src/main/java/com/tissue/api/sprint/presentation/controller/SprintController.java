package com.tissue.api.sprint.presentation.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tissue.api.common.dto.ApiResponse;
import com.tissue.api.common.dto.PageResponse;
import com.tissue.api.security.authentication.interceptor.LoginRequired;
import com.tissue.api.security.authorization.interceptor.RoleRequired;
import com.tissue.api.sprint.presentation.condition.SprintIssueSearchCondition;
import com.tissue.api.sprint.presentation.condition.SprintSearchCondition;
import com.tissue.api.sprint.presentation.dto.request.AddSprintIssuesRequest;
import com.tissue.api.sprint.presentation.dto.request.CreateSprintRequest;
import com.tissue.api.sprint.presentation.dto.request.RemoveSprintIssueRequest;
import com.tissue.api.sprint.presentation.dto.request.UpdateSprintRequest;
import com.tissue.api.sprint.presentation.dto.request.UpdateSprintStatusRequest;
import com.tissue.api.sprint.presentation.dto.response.AddSprintIssuesResponse;
import com.tissue.api.sprint.presentation.dto.response.CreateSprintResponse;
import com.tissue.api.sprint.presentation.dto.response.SprintDetail;
import com.tissue.api.sprint.presentation.dto.response.SprintIssueDetail;
import com.tissue.api.sprint.presentation.dto.response.UpdateSprintResponse;
import com.tissue.api.sprint.presentation.dto.response.UpdateSprintStatusResponse;
import com.tissue.api.sprint.service.command.SprintCommandService;
import com.tissue.api.sprint.service.query.SprintQueryService;
import com.tissue.api.workspacemember.domain.WorkspaceRole;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/workspaces/{workspaceCode}/sprints")
public class SprintController {

	private final SprintCommandService sprintCommandService;
	private final SprintQueryService sprintQueryService;

	@LoginRequired
	@ResponseStatus(HttpStatus.CREATED)
	@RoleRequired(role = WorkspaceRole.MEMBER)
	@PostMapping
	public ApiResponse<CreateSprintResponse> createSprint(
		@PathVariable String workspaceCode,
		@RequestBody @Valid CreateSprintRequest request
	) {
		CreateSprintResponse response = sprintCommandService.createSprint(
			workspaceCode,
			request
		);
		return ApiResponse.ok("Sprint created.", response);
	}

	/**
	 * Todo
	 *  - updateSprintContent과 updateSprintDate 통합하기
	 *  - 굳이 분리하지 않아도 될듯
	 */
	@LoginRequired
	@RoleRequired(role = WorkspaceRole.MEMBER)
	@PatchMapping("/{sprintKey}")
	public ApiResponse<UpdateSprintResponse> updateSprint(
		@PathVariable String workspaceCode,
		@PathVariable String sprintKey,
		@RequestBody @Valid UpdateSprintRequest request
	) {
		UpdateSprintResponse response = sprintCommandService.updateSprint(
			workspaceCode,
			sprintKey,
			request
		);
		return ApiResponse.ok("Sprint updated.", response);
	}

	@LoginRequired
	@RoleRequired(role = WorkspaceRole.MEMBER)
	@PostMapping("/{sprintKey}/issues")
	public ApiResponse<AddSprintIssuesResponse> addIssues(
		@PathVariable String workspaceCode,
		@PathVariable String sprintKey,
		@RequestBody @Valid AddSprintIssuesRequest request
	) {
		AddSprintIssuesResponse response = sprintCommandService.addIssues(
			workspaceCode,
			sprintKey,
			request
		);
		return ApiResponse.ok("Issues added to sprint.", response);
	}

	@LoginRequired
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RoleRequired(role = WorkspaceRole.MEMBER)
	@DeleteMapping("/{sprintKey}/issues")
	public ApiResponse<Void> removeIssue(
		@PathVariable String workspaceCode,
		@PathVariable String sprintKey,
		@RequestBody @Valid RemoveSprintIssueRequest request
	) {
		sprintCommandService.removeIssue(
			workspaceCode,
			sprintKey,
			request
		);
		return ApiResponse.okWithNoContent("Issue removed from sprint.");
	}

	@LoginRequired
	@RoleRequired(role = WorkspaceRole.MEMBER)
	@PatchMapping("/{sprintKey}/status")
	public ApiResponse<UpdateSprintStatusResponse> updateSprintStatus(
		@PathVariable String workspaceCode,
		@PathVariable String sprintKey,
		@RequestBody @Valid UpdateSprintStatusRequest request
	) {
		UpdateSprintStatusResponse response = sprintCommandService.updateSprintStatus(
			workspaceCode,
			sprintKey,
			request
		);
		return ApiResponse.ok("Sprint status updated.", response);
	}

	@LoginRequired
	@RoleRequired(role = WorkspaceRole.VIEWER)
	@GetMapping
	public ApiResponse<PageResponse<SprintDetail>> getSprints(
		@PathVariable String workspaceCode,
		SprintSearchCondition sprintSearchCondition,
		@PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
	) {
		Page<SprintDetail> page = sprintQueryService.getSprints(
			workspaceCode,
			sprintSearchCondition,
			pageable
		);
		return ApiResponse.ok("Found sprints.", PageResponse.of(page));
	}

	@LoginRequired
	@RoleRequired(role = WorkspaceRole.VIEWER)
	@GetMapping("/{sprintKey}")
	public ApiResponse<SprintDetail> getSprintDetail(
		@PathVariable String workspaceCode,
		@PathVariable String sprintKey
	) {
		SprintDetail response = sprintQueryService.getSprintDetail(
			workspaceCode,
			sprintKey
		);
		return ApiResponse.ok("Found sprint.", response);
	}

	@LoginRequired
	@RoleRequired(role = WorkspaceRole.VIEWER)
	@GetMapping("/{sprintKey}/issues")
	public ApiResponse<PageResponse<SprintIssueDetail>> getSprintIssues(
		@PathVariable String workspaceCode,
		@PathVariable String sprintKey,
		SprintIssueSearchCondition searchCondition,
		@PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
	) {
		Page<SprintIssueDetail> page = sprintQueryService.getSprintIssues(
			workspaceCode,
			sprintKey,
			searchCondition,
			pageable
		);
		return ApiResponse.ok("Found issues in sprint.", PageResponse.of(page));
	}
}
