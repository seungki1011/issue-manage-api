package com.tissue.api.workspace.domain;

import java.util.ArrayList;
import java.util.List;

import com.tissue.api.common.entity.BaseEntity;
import com.tissue.api.member.domain.Member;
import com.tissue.api.position.domain.Position;
import com.tissue.api.workspacemember.domain.WorkspaceMember;
import com.tissue.api.common.ColorType;
import com.tissue.api.invitation.domain.Invitation;
import com.tissue.api.issue.domain.Issue;
import com.tissue.api.workspace.exception.InvalidMemberCountException;
import com.tissue.api.workspace.exception.WorkspaceMemberLimitExceededException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Workspace extends BaseEntity {

	// Todo: 추후 낙관적 락 적용
	// @Version
	// private Long version;

	/**
	 * 다음 링크의 주석을 확인
	 * {@link Member#MAX_MY_WORKSPACE_COUNT}
	 */
	private static final int MAX_MEMBER_COUNT = 500;

	/**
	 * Todo
	 *  - memberCount에 캐시 적용 고려
	 *  -> memberCount 증가/감소가 들어가는 로직은 전부 예외를 잡고 재수행 로직을 적용해야 한다
	 *  -> spring-retry 사용(AOP로 직접 구현해도 되지만 귀찮음)
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "WORKSPACE_ID")
	private Long id;

	@Column(unique = true, nullable = false)
	private String code;

	@Column(nullable = false)
	private String name;
	@Column(nullable = false)
	private String description;

	private String password;

	@Column(nullable = false)
	private int memberCount = 0;

	@OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Position> positions = new ArrayList<>();

	@OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<WorkspaceMember> workspaceMembers = new ArrayList<>();

	@OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Invitation> invitations = new ArrayList<>();

	@OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Issue> issues = new ArrayList<>();

	@Builder
	public Workspace(String code, String name, String description, String password) {
		this.code = code;
		this.name = name;
		this.description = description;
		this.password = password;
	}

	public Position createPosition(String name, String description, ColorType color) {
		return Position.builder()
			.name(name)
			.description(description)
			.color(color)
			.workspace(this)
			.build();
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void updatePassword(String password) {
		this.password = password;
	}

	public void updateName(String name) {
		this.name = name;
	}

	public void updateDescription(String description) {
		this.description = description;
	}

	public void increaseMemberCount() {
		validateMemberLimit();
		this.memberCount++;
	}

	public void decreaseMemberCount() {
		validatePositiveMemberCount();
		this.memberCount--;
	}

	private void validateMemberLimit() {
		if (this.memberCount >= MAX_MEMBER_COUNT) {
			throw new WorkspaceMemberLimitExceededException();
		}
	}

	private void validatePositiveMemberCount() {
		if (this.memberCount <= 0) {
			throw new InvalidMemberCountException();
		}
	}
}
