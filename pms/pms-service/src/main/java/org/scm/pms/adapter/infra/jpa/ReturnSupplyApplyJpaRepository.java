    package org.scm.pms.adapter.infra.jpa;

    import org.scm.pms.adapter.infra.domain.ReturnSupplyApply;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;

    @Repository
    public interface ReturnSupplyApplyJpaRepository extends JpaRepository<ReturnSupplyApply, Long> {
        // 可根据 applyNo 查询退供申请
        ReturnSupplyApply findByApplyNo(String applyNo);
    }
