package com.tissue.api.notification.domain;

import java.util.UUID;

import com.tissue.api.common.entity.BaseDateEntity;
import com.tissue.api.notification.domain.enums.NotificationType;
import com.tissue.api.notification.domain.vo.EntityReference;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
	uniqueConstraints = {
		@UniqueConstraint(
			name = "UK_EVENT_RECEIVER",
			columnNames = {"eventId", "receiverWorkspaceMemberId"})
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseDateEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "event_id", nullable = false)
	private UUID eventId;

	@Column(nullable = false)
	private Long receiverWorkspaceMemberId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NotificationType type;

	@Embedded
	private EntityReference entityReference;

	// TODO: title, message를 VO로 묶기?
	@Column(nullable = false)
	private String title;

	@Column(length = 1000)
	private String message;

	@Column(nullable = false)
	private Long actorWorkspaceMemberId;

	private String actorWorkspaceMemberNickname;

	@Column(nullable = false)
	private boolean isRead;

	@Builder
	public Notification(
		UUID eventId,
		NotificationType notificationType,
		EntityReference entityReference,
		Long actorWorkspaceMemberId,
		String actorWorkspaceMemberNickname,
		Long receiverWorkspaceMemberId,
		String title,
		String message
	) {
		this.eventId = eventId;
		this.type = notificationType;
		this.entityReference = entityReference;
		this.actorWorkspaceMemberId = actorWorkspaceMemberId;
		this.actorWorkspaceMemberNickname = actorWorkspaceMemberNickname;
		this.receiverWorkspaceMemberId = receiverWorkspaceMemberId;
		this.title = title;
		this.message = message;
		this.isRead = false;
	}

	public void markAsRead() {
		this.isRead = true;
	}
}
