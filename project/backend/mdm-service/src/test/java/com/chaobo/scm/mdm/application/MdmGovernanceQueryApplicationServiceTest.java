package com.chaobo.scm.mdm.application;

import com.chaobo.scm.mdm.infrastructure.persistence.MdmGovernanceQueryMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MdmGovernanceQueryApplicationServiceTest {

    @Test
    void returnsPagedApprovalAndChangeLogReadModels() {
        MdmGovernanceQueryApplicationService service = new MdmGovernanceQueryApplicationService(new StubMapper());

        var approvals = service.approvals("SKU", 2, 1, 20);
        var changes = service.changeLogs("SKU", "SKU-1", 1, 20);

        assertThat(approvals.total()).isEqualTo(1);
        assertThat(approvals.items()).extracting(MdmGovernanceQueryMapper.ApprovalView::recordNo)
                .containsExactly("MDR-1");
        assertThat(changes.items()).extracting(MdmGovernanceQueryMapper.ChangeLogView::changeNo)
                .containsExactly("MDV-1");
    }

    @Test
    void rejectsUnboundedPageSizes() {
        MdmGovernanceQueryApplicationService service = new MdmGovernanceQueryApplicationService(new StubMapper());

        assertThatThrownBy(() -> service.approvals(null, null, 1, 101))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static class StubMapper implements MdmGovernanceQueryMapper {

        @Override
        public List<ApprovalView> listApprovals(String typeCode, Integer status, int limit, int offset) {
            return List.of(new ApprovalView("MDR-1", "SKU", "SKU-1", "商品一", 2,
                    "待审核", 3, LocalDateTime.now()));
        }

        @Override
        public long countApprovals(String typeCode, Integer status) {
            return 1;
        }

        @Override
        public List<ChangeLogView> listChangeLogs(String typeCode, String dataCode, int limit, int offset) {
            return List.of(new ChangeLogView("MDV-1", "MDR-1", "SKU", "SKU-1", 1,
                    "初始版本", LocalDateTime.now()));
        }

        @Override
        public long countChangeLogs(String typeCode, String dataCode) {
            return 1;
        }
    }
}
