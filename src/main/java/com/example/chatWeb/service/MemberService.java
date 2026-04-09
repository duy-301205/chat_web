package com.example.chatWeb.service;

import com.example.chatWeb.dto.request.AddMemberRequest;
import com.example.chatWeb.entity.ConversationMember;
import com.example.chatWeb.repository.ConversationMemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final ConversationMemberRepository memberRepository;

    @Transactional
    public void addMember(AddMemberRequest addMemberRequest) {
        ConversationMember member = new ConversationMember();
        member.setConversation(addMemberRequest.getConversation());
        member.setUser(addMemberRequest.getUser());
        member.setRole(addMemberRequest.getRole());
        member.setJoinedAt(OffsetDateTime.now());

        memberRepository.save(member);
    }
}
