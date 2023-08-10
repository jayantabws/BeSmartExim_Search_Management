package com.besmartexim.database.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

import com.besmartexim.database.entity.UserSearch;
import com.besmartexim.dto.response.ExpInd;


@Repository
public interface UserSearchRepository extends JpaRepository<UserSearch, Long> {
	
	/*
	@Procedure(value = "ListSearchResult")
	List<ExpInd> getListOfExportIND(String searchType,String tradeType,String frmDate, String toDate,String searchBy,
																	String searchValue, String countryCode,Integer pageNo,Integer numberOfRecords);
*/
	
	List<UserSearch> findTop5ByCreatedByOrderByCreatedDateDesc(Long createdBy);
	List<UserSearch> findAllByCreatedByOrderByCreatedDateDesc(Long createdBy);
	// List<UserSearch> findAllOrderByCreatedDateDesc();
	List<UserSearch> findByCreatedByOrderByCreatedDateDesc(Long userId);
	List<UserSearch> findAllByOrderByCreatedDateDesc();
	
	@Query(nativeQuery = true, value="SELECT * FROM user_search where created_by = :uplineId or created_by in (select id from users where upline_id = :uplineId) order by created_date desc")
	List<UserSearch> findByUplineIdOrderByCreatedDateDesc(Long uplineId);
	
	
	
	
}
