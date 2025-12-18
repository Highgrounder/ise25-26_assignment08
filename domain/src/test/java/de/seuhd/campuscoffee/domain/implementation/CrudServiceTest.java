package de.seuhd.campuscoffee.domain.implementation;

import de.seuhd.campuscoffee.domain.exceptions.DuplicationException;
import de.seuhd.campuscoffee.domain.model.objects.DomainModel;
import de.seuhd.campuscoffee.domain.ports.data.CrudDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link CrudServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
public class CrudServiceTest {

    @Mock
    private CrudDataService<DomainModel<Long>, Long> dataService;

    private CrudServiceImpl<DomainModel<Long>, Long> crudService;

    @BeforeEach
    void beforeEach() {
        crudService = new CrudServiceImpl<DomainModel<Long>, Long>((Class) DomainModel.class) {
            @Override
            protected CrudDataService<DomainModel<Long>, Long> dataService() {
                return dataService;
            }
        };
    }


    @Test
    void clearDelegatesToDataService() {
        crudService.clear();
        verify(dataService).clear();
    }


    @Test
    void getAllReturnsAllEntities() {
        List<DomainModel<Long>> list = List.of(mock(DomainModel.class));

        when(dataService.getAll()).thenReturn(list);

        List<DomainModel<Long>> result = crudService.getAll();

        assertThat(result).isEqualTo(list);
        verify(dataService).getAll();
    }


    @Test
    void getByIdReturnsEntity() {
        DomainModel<Long> domain = mock(DomainModel.class);

        when(dataService.getById(1L)).thenReturn(domain);

        DomainModel<Long> result = crudService.getById(1L);

        assertThat(result).isEqualTo(domain);
        verify(dataService).getById(1L);
    }


    @Test
    void upsertCreatesNewEntityWhenIdIsNull() {
        DomainModel<Long> domain = mock(DomainModel.class);
        DomainModel<Long> saved = mock(DomainModel.class);

        when(domain.getId()).thenReturn(null);
        when(saved.getId()).thenReturn(1L);
        when(dataService.upsert(domain)).thenReturn(saved);

        DomainModel<Long> result = crudService.upsert(domain);

        assertThat(result.getId()).isEqualTo(1L);
        verify(dataService).upsert(domain);
        verify(dataService, never()).getById(any());
    }


    @Test
    void upsertUpdatesExistingEntityWhenIdExists() {
        DomainModel<Long> domain = mock(DomainModel.class);

        when(domain.getId()).thenReturn(1L);
        when(dataService.getById(1L)).thenReturn(domain);
        when(dataService.upsert(domain)).thenReturn(domain);

        DomainModel<Long> result = crudService.upsert(domain);

        assertThat(result).isEqualTo(domain);
        verify(dataService).getById(1L);
        verify(dataService).upsert(domain);
    }

    @Test
    void upsertThrowsDuplicationException() {
        DomainModel<Long> domain = mock(DomainModel.class);

        when(domain.getId()).thenReturn(null);
        when(dataService.upsert(domain))
                .thenThrow(new DuplicationException(DomainModel.class, "id", "duplicate"));

        assertThrows(DuplicationException.class, () -> crudService.upsert(domain));
        verify(dataService).upsert(domain);
    }


    @Test
    void deleteDelegatesToDataService() {
        crudService.delete(1L);
        verify(dataService).delete(1L);
    }
}