package hexlet.code.app.component.specification;

import hexlet.code.app.dto.task.TaskParamsDTO;
import hexlet.code.app.model.Task;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public final class TaskSpecification {

    public Specification<Task> build(TaskParamsDTO params) {
        return withAssigneeId(params.getAssigneeId())
                .and(withStatus(params.getStatus()))
                .and(withLabelId(params.getLabelId()))
                .and(withTitleContains(params.getTitleCont()));
    }

    private Specification<Task> withTitleContains(String mayContain) {
        return (root, query, criteriaBuilder) -> mayContain == null ? criteriaBuilder.conjunction()
                : criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + mayContain + "%");
    }

    private Specification<Task> withAssigneeId(Long assigneeId) {
        return (root, query, cb) -> assigneeId == null ? cb.conjunction()
                : cb.equal(root.get("assignee").get("id"), assigneeId);
    }

    private Specification<Task> withStatus(String status) {
        return (root, query, cb) -> status == null ? cb.conjunction()
                : cb.equal(root.get("taskStatus").get("slug"), status);
    }

    private Specification<Task> withLabelId(Long labelId) {
        return (root, query, cb) -> labelId == null ? cb.conjunction()
                : cb.equal(root.joinSet("labels").get("id"), labelId);
    }
}
