package com.knupbackend.session.dto.request;

import java.util.List;

/** 일괄/전체 강퇴 요청. participantIds 가 비어 있으면 전체 강퇴. */
public record KickParticipantsRequest(List<String> participantIds) {}
