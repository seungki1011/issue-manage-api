package com.uranus.taskmanager.api.member.service.command;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.uranus.taskmanager.api.member.domain.Member;
import com.uranus.taskmanager.api.member.exception.DuplicateEmailException;
import com.uranus.taskmanager.api.member.exception.OwnedWorkspaceExistsException;
import com.uranus.taskmanager.api.member.presentation.dto.request.SignupMemberRequest;
import com.uranus.taskmanager.api.member.presentation.dto.request.UpdateMemberEmailRequest;
import com.uranus.taskmanager.api.member.presentation.dto.request.UpdateMemberPasswordRequest;
import com.uranus.taskmanager.api.member.presentation.dto.request.WithdrawMemberRequest;
import com.uranus.taskmanager.api.member.presentation.dto.response.SignupMemberResponse;
import com.uranus.taskmanager.api.member.presentation.dto.response.UpdateMemberEmailResponse;
import com.uranus.taskmanager.api.security.authentication.exception.InvalidLoginPasswordException;
import com.uranus.taskmanager.api.workspace.domain.Workspace;
import com.uranus.taskmanager.api.workspacemember.domain.WorkspaceMember;
import com.uranus.taskmanager.api.workspacemember.domain.WorkspaceRole;
import com.uranus.taskmanager.helper.ServiceIntegrationTestHelper;

class MemberCommandServiceIT extends ServiceIntegrationTestHelper {

	@AfterEach
	public void tearDown() {
		databaseCleaner.execute();
	}

	@Test
	@DisplayName("회원 가입에 성공하면 멤버가 저장된다")
	void signup_sucess_memberIsSaved() {
		// given
		SignupMemberRequest signupMemberRequest = SignupMemberRequest.builder()
			.loginId("testuser")
			.password("testpassword1234!")
			.email("testemail@test.com")
			.build();

		// when
		SignupMemberResponse signupMemberResponse = memberCommandService.signup(signupMemberRequest);

		// then
		assertThat(signupMemberResponse.getMemberId()).isEqualTo(1L);
		assertThat(memberRepository.findById(1L).get().getEmail()).isEqualTo("testemail@test.com");
	}

	@Test
	@DisplayName("회원 가입에 성공하여 저장된 멤버의 패스워드는 암호화 되어 있다")
	void signup_sucess_memberPasswordIsEncrypted() {
		// given
		SignupMemberRequest signupMemberRequest = SignupMemberRequest.builder()
			.loginId("testuser")
			.password("testpassword1234!")
			.email("testemail@test.com")
			.build();

		// when
		memberCommandService.signup(signupMemberRequest);

		// then
		Optional<Member> member = memberRepository.findByLoginId("testuser");
		String encodedPassword = member.get().getPassword();
		assertThat(passwordEncoder.matches("testpassword1234!", encodedPassword)).isTrue();
	}

	@Test
	@DisplayName("회원 가입 시 입력한 패스워드와 암호화한 패스워드는 서로 다르다")
	void signup_sucess_requestPaswordMustBeDifferentWithEncryptedPassword() {
		// given
		SignupMemberRequest signupMemberRequest = SignupMemberRequest.builder()
			.loginId("testuser")
			.password("testpassword1234!")
			.email("testemail@test.com")
			.build();

		// when
		memberCommandService.signup(signupMemberRequest);

		// then
		Optional<Member> member = memberRepository.findByLoginId("testuser");
		String encodedPassword = member.get().getPassword();
		assertThat(encodedPassword).isNotEqualTo("testpassword1234!");
	}

	@Test
	@DisplayName("이메일 업데이트를 성공하면 이메일 업데이트 응답을 반환한다")
	void updateEmail_success_returnsMemberEmailUpdateResponse() {
		// given
		Member member = memberRepository.save(Member.builder()
			.loginId("member1")
			.email("member1@test.com")
			.password(passwordEncoder.encode("password1234!"))
			.build());

		String newEmail = "newemail@test.com";
		UpdateMemberEmailRequest request = new UpdateMemberEmailRequest(newEmail);

		// when
		UpdateMemberEmailResponse response = memberCommandService.updateEmail(request, member.getId());

		// then
		assertThat(response.getMemberId()).isEqualTo(member.getId());

		Member updatedMember = memberRepository.findById(member.getId()).get();
		assertThat(updatedMember.getEmail()).isEqualTo(newEmail);
	}

	@Test
	@DisplayName("이메일 업데이트 시 이메일이 중복되면 예외가 발생한다")
	void updateEmail_throwsException_whenEmailDuplicated() {
		// given
		Member existingMember = memberRepository.save(
			Member.builder()
				.loginId("member1")
				.email("member1@test.com")
				.password(passwordEncoder.encode("password1"))
				.build()
		);

		UpdateMemberEmailRequest request = new UpdateMemberEmailRequest(existingMember.getEmail());

		// when & then
		assertThatThrownBy(() -> memberCommandService.updateEmail(request, existingMember.getId()))
			.isInstanceOf(DuplicateEmailException.class);
	}

	@Test
	@DisplayName("패스워드 업데이트를 성공하면 아무것도 반환하지 않는다")
	void updatePassword_success_returnsNothing() {
		// given
		Member member = memberRepository.save(Member.builder()
			.loginId("member1")
			.email("member1@test.com")
			.password(passwordEncoder.encode("password1234!"))
			.build());

		String newPassword = "newpassword1234!";
		UpdateMemberPasswordRequest request = new UpdateMemberPasswordRequest(newPassword);

		// when & then
		assertThatNoException().isThrownBy(() -> memberCommandService.updatePassword(request, member.getId()));
	}

	@Test
	@DisplayName("패스워드 업데이트를 성공하면 업데이트 된 멤버는 암호화된 새로운 패스워드를 가진다")
	void updatePassword_success_updatedMemberHasNewEncrytedPassword() {
		// given
		Member member = memberRepository.save(Member.builder()
			.loginId("member1")
			.email("member1@test.com")
			.password(passwordEncoder.encode("password1234!"))
			.build());

		String newPassword = "newpassword1234!";
		UpdateMemberPasswordRequest request = new UpdateMemberPasswordRequest(newPassword);

		// when
		memberCommandService.updatePassword(request, member.getId());

		// then
		Member updatedMember = memberRepository.findById(member.getId()).get();
		assertThat(passwordEncoder.matches(newPassword, updatedMember.getPassword())).isTrue();
	}

	@Test
	@DisplayName("멤버 탈퇴에 성공하면 해당 멤버는 삭제된다")
	void withdrawMember_success_memberIsDeleted() {
		// given
		Member member = memberRepository.save(Member.builder()
			.loginId("member1")
			.email("member1@test.com")
			.password(passwordEncoder.encode("password1234!"))
			.build());

		WithdrawMemberRequest request = new WithdrawMemberRequest("password1234!");

		// when
		memberCommandService.withdraw(request, member.getId());

		// then
		assertThat(memberRepository.findById(member.getId())).isEmpty();
	}

	@Test
	@DisplayName("멤버 탈퇴에 요청의 패스워드가 멤버 패스워드와 일치하지 않으면 예외가 발생한다")
	void withdrawMember_throwsException_ifRequestPasswordsIsNotValid() {
		// given
		Member member = memberRepository.save(Member.builder()
			.loginId("member1")
			.email("member1@test.com")
			.password(passwordEncoder.encode("password1234!"))
			.build());

		WithdrawMemberRequest request = new WithdrawMemberRequest("invalidPassword");

		// when & then
		assertThatThrownBy(() -> memberCommandService.withdraw(request, member.getId())).isInstanceOf(
			InvalidLoginPasswordException.class);
	}

	@Test
	@DisplayName("멤버 탈퇴에 요청 시 워크스페이스 소유자(OWNER)로 등록되어 있으면 예외가 발생한다")
	void withdrawMember_throwsException_ifRequesterIsOwnerOfWorkspace() {
		// given
		Member member = memberRepository.save(Member.builder()
			.loginId("member1")
			.email("member1@test.com")
			.password(passwordEncoder.encode("password1234!"))
			.build());

		Workspace workspace = workspaceRepository.save(Workspace.builder()
			.code("TESTCODE")
			.name("workspace1")
			.description("description1")
			.build());

		workspaceMemberRepository.save(WorkspaceMember.addWorkspaceMember(member, workspace, WorkspaceRole.OWNER,
			member.getEmail()));

		WithdrawMemberRequest request = new WithdrawMemberRequest("password1234!");

		// when & then
		assertThatThrownBy(() -> memberCommandService.withdraw(request, member.getId())).isInstanceOf(
			OwnedWorkspaceExistsException.class);
	}
}