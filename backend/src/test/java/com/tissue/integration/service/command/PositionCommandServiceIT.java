package com.tissue.integration.service.command;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import com.tissue.api.common.enums.ColorType;
import com.tissue.api.common.exception.type.InvalidOperationException;
import com.tissue.api.member.domain.Member;
import com.tissue.api.position.domain.Position;
import com.tissue.api.position.presentation.dto.request.CreatePositionRequest;
import com.tissue.api.position.presentation.dto.request.UpdatePositionColorRequest;
import com.tissue.api.position.presentation.dto.request.UpdatePositionRequest;
import com.tissue.api.position.presentation.dto.response.CreatePositionResponse;
import com.tissue.api.position.presentation.dto.response.UpdatePositionColorResponse;
import com.tissue.api.position.presentation.dto.response.UpdatePositionResponse;
import com.tissue.api.workspace.domain.Workspace;
import com.tissue.api.workspace.exception.WorkspaceNotFoundException;
import com.tissue.api.workspacemember.domain.WorkspaceMember;
import com.tissue.api.workspacemember.domain.WorkspaceRole;
import com.tissue.support.helper.ServiceIntegrationTestHelper;

class PositionCommandServiceIT extends ServiceIntegrationTestHelper {

	private Workspace workspace;

	@BeforeEach
	void setUp() {
		// create workspace
		workspace = testDataFixture.createWorkspace("test workspace", null, null);
	}

	@AfterEach
	public void tearDown() {
		databaseCleaner.execute();
	}

	@Test
	@DisplayName("특정 워크스페이스에 대해 포지션을 생성할 수 있다")
	void canCreatePosition() {
		// Given
		CreatePositionRequest request = new CreatePositionRequest(
			"Developer",
			"Backend Developer"
		);

		// When
		CreatePositionResponse createResponse = positionCommandService.createPosition(workspace.getCode(), request);

		// Then
		assertThat(createResponse.name()).isEqualTo("Developer");
		assertThat(createResponse.description()).isEqualTo("Backend Developer");
		assertThat(createResponse.color()).isNotNull();
	}

	@Test
	@DisplayName("포지션을 생성하면 포지션이 사용하지 않은 색상 중 랜덤한 색을 배정받는다")
	void createPosition_RandomColorAssigned() {
		// Given
		CreatePositionRequest request = new CreatePositionRequest(
			"Developer",
			"Backend Developer"
		);

		// When
		CreatePositionResponse createResponse = positionCommandService.createPosition(workspace.getCode(), request);

		// Then
		Position findPosition = positionRepository.findById(createResponse.positionId()).orElseThrow();

		assertThat(findPosition.getColor()).isNotNull();
		assertThat(findPosition.getColor()).isInstanceOf(ColorType.class);
	}

	@Test
	@DisplayName("포지션의 이름과 설명을 수정할 수 있다")
	void canUpdatePositionNameAndDescription() {
		// Given
		Position position = positionRepository.save(Position.builder()
			.name("Developer")
			.description("Backend Developer")
			.color(ColorType.BLACK)
			.workspace(workspace)
			.build()
		);

		UpdatePositionRequest request = new UpdatePositionRequest(
			"Senior Developer",
			"Senior Backend Developer"
		);

		// When
		UpdatePositionResponse response = positionCommandService.updatePosition(
			workspace.getCode(),
			position.getId(),
			request
		);

		// Then
		assertThat(response.name()).isEqualTo("Senior Developer");
		assertThat(response.description()).isEqualTo("Senior Backend Developer");
	}

	@Test
	@DisplayName("포지션의 색을 제공하는 색상 중 하나로 수정할 수 있다")
	void canUpdatePositionColor() {
		// given
		Position position = positionRepository.save(Position.builder()
			.name("Developer")
			.description("Backend Developer")
			.color(ColorType.BLACK)
			.workspace(workspace)
			.build()
		);

		UpdatePositionColorRequest request = new UpdatePositionColorRequest(ColorType.GREEN);

		// when
		UpdatePositionColorResponse response = positionCommandService.updatePositionColor(
			workspace.getCode(),
			position.getId(),
			request
		);

		// then
		assertThat(response.color()).isEqualTo(ColorType.GREEN);
	}

	@Test
	@Transactional
	@DisplayName("사용중인 포지션은 삭제할 수 없다")
	void cannotDeletePositionInUse() {
		// Given
		Position position = positionRepository.save(Position.builder()
			.name("Developer")
			.description("Backend Developer")
			.color(ColorType.BLACK)
			.workspace(workspace)
			.build()
		);

		Member member = testDataFixture.createMember("testuser");

		WorkspaceMember workspaceMember = testDataFixture.createWorkspaceMember(
			member,
			workspace,
			WorkspaceRole.MEMBER
		);

		workspaceMember.addPosition(position);

		// When & Then
		assertThatThrownBy(() -> positionCommandService.deletePosition(workspace.getCode(), position.getId()))
			.isInstanceOf(InvalidOperationException.class);
	}

	@Test
	@DisplayName("존재하지 않는 워크스페이스에 대해 포지션을 생성할 수 없다")
	void cannotCreatePositionWithNonExistingWorkspace() {
		// Given
		String nonExistentCode = "INVALID";
		CreatePositionRequest request = new CreatePositionRequest("Developer", "Backend Developer");

		// When & Then
		assertThatThrownBy(() -> positionCommandService.createPosition(nonExistentCode, request))
			.isInstanceOf(WorkspaceNotFoundException.class);
	}
}
