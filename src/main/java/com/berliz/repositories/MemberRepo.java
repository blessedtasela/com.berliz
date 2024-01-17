
package com.berliz.repositories;

import com.berliz.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepo extends JpaRepository<Member, Integer> {

    Member findByUser(User user);

    Member findByUserId(Integer id);

    List<Member> getActiveMembers();

    Member findByMemberId(Integer id);

    List<Member> getMyMembersByCenter(Center center);

    List<Member>getMyActiveMembersByCenter(Center center);

//    Member findByPartnerId(Integer id);

    Integer countCenterMembersByEmail(String email);
}
