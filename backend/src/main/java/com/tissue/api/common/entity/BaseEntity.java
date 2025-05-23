package com.tissue.api.common.entity;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity extends BaseDateEntity {

	@CreatedBy
	@Column(updatable = false)
	private Long createdBy;

	@LastModifiedBy
	private Long lastModifiedBy;

	// @Column(nullable = false)
	// private boolean deleted = false;
	//
	// private LocalDateTime deletedAt;
	//
	// public void softDelete() {
	// 	this.deleted = true;
	// 	this.deletedAt = LocalDateTime.now();
	// }
	//
	// public void restore() {
	// 	if (this.deleted) {
	// 		this.deleted = false;
	// 		this.deletedAt = null;
	// 	}
	// }
}
