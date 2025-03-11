package hexlet.code.app.mapper;

import org.mapstruct.MappingTarget;

public interface BaseMapper<T, DTO, CreateDTO, UpdateDTO> {
    DTO mapToDTO(T entity);
    T mapToEntity(CreateDTO createDTO);
    void update(UpdateDTO updateDTO, @MappingTarget T entity);
}
