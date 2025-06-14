package com.tissue.unit.service;

import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tissue.api.issue.application.service.reader.IssueReader;
import com.tissue.api.issue.domain.event.IssueCreatedEvent;
import com.tissue.api.issue.domain.model.Issue;
import com.tissue.api.issue.domain.model.enums.IssueType;
import com.tissue.api.member.domain.model.Member;
import com.tissue.api.notification.application.eventhandler.NotificationEventHandler;
import com.tissue.api.notification.application.service.command.NotificationCommandService;
import com.tissue.api.notification.application.service.command.NotificationProcessor;
import com.tissue.api.notification.application.service.command.NotificationTargetService;
import com.tissue.api.notification.domain.model.Notification;
import com.tissue.api.notification.domain.model.vo.NotificationMessage;
import com.tissue.api.notification.domain.service.message.NotificationMessageFactory;
import com.tissue.api.notification.infrastructure.repository.ActivityLogRepository;
import com.tissue.api.workspacemember.application.service.command.WorkspaceMemberReader;
import com.tissue.api.workspacemember.domain.model.WorkspaceMember;
import com.tissue.api.workspacemember.infrastructure.repository.WorkspaceMemberRepository;

@ExtendWith(MockitoExtension.class)
class NotificationEventHandlerTest {

	@Mock
	private NotificationCommandService notificationService;

	@Mock
	private IssueReader issueReader;

	@Mock
	private WorkspaceMemberReader workspaceMemberReader;

	@Mock
	private WorkspaceMemberRepository workspaceMemberRepository;

	@Mock
	private NotificationTargetService targetService;

	@Mock
	private NotificationProcessor notificationProcessor;

	@Mock
	private NotificationMessageFactory notificationMessageFactory;

	@Mock
	private ActivityLogRepository activityLogRepository;

	@InjectMocks
	private NotificationEventHandler notificationEventHandler;

	@Test
	@DisplayName("이슈 생성 이벤트 발생 시 워크스페이스 멤버들에게 알림이 처리되어야 함")
	void handleIssueCreated_ShouldProcessNotificationForAllWorkspaceMembers() {
		// given
		String workspaceCode = "TESTCODE";
		Long actorId = 123L;

		Issue issue = mock(Issue.class);
		when(issue.getIssueKey()).thenReturn("ISSUE-1");
		when(issue.getWorkspaceCode()).thenReturn(workspaceCode);
		when(issue.getType()).thenReturn(IssueType.STORY);

		IssueCreatedEvent event = IssueCreatedEvent.createEvent(issue, actorId);

		WorkspaceMember wm1 = mock(WorkspaceMember.class);
		WorkspaceMember wm2 = mock(WorkspaceMember.class);
		when(wm1.getMember()).thenReturn(mock(Member.class));
		when(wm2.getMember()).thenReturn(mock(Member.class));
		List<WorkspaceMember> members = List.of(wm1, wm2);

		when(targetService.getWorkspaceWideMemberTargets(workspaceCode)).thenReturn(members);

		NotificationMessage msg = new NotificationMessage("title", "body");
		when(notificationMessageFactory.createMessage(event)).thenReturn(msg);

		Notification notification1 = mock(Notification.class);
		Notification notification2 = mock(Notification.class);

		when(notificationService.createNotification(eq(event), anyLong(), eq(msg)))
			.thenReturn(notification1, notification2);

		// when
		notificationEventHandler.handleIssueCreated(event);

		// then
		verify(notificationService, times(2)).createNotification(eq(event), anyLong(), eq(msg));
		verify(notificationProcessor, times(2)).process(any(Notification.class));
	}
}