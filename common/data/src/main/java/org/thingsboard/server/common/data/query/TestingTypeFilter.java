package org.thingsboard.server.common.data.query;

import lombok.Data;

@Data

public class TestingTypeFilter implements EntityFilter{
    @Override
    public EntityFilterType getType() {
        return EntityFilterType.TESTING_TYPE;
    }

    private String testingType;

    private String testingNameFilter;
}
