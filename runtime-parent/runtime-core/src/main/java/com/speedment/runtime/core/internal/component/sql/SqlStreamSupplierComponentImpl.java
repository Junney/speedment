/**
 *
 * Copyright (c) 2006-2017, Speedment, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.speedment.runtime.core.internal.component.sql;

import com.speedment.common.injector.annotation.Inject;
import com.speedment.runtime.config.identifier.TableIdentifier;
import com.speedment.runtime.core.component.DbmsHandlerComponent;
import com.speedment.runtime.core.component.ManagerComponent;
import com.speedment.runtime.core.component.ProjectComponent;
import com.speedment.runtime.core.component.sql.SqlStreamSupplierComponent;
import com.speedment.runtime.core.db.SqlFunction;
import com.speedment.runtime.core.stream.parallel.ParallelStrategy;

import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * The default implementation of the 
 * {@link SqlStreamSupplierComponent}-interface.
 * 
 * @author  Per Minborg
 * @since   3.0.1
 */
public final class SqlStreamSupplierComponentImpl implements SqlStreamSupplierComponent {

    private final Map<TableIdentifier<?>, SqlStreamSupplier<?>> supportMap;
    
    private @Inject ProjectComponent projectComponent;
    private @Inject DbmsHandlerComponent dbmsHandlerComponent;
    private @Inject ManagerComponent managerComponent;

    public SqlStreamSupplierComponentImpl() {
        this.supportMap = new ConcurrentHashMap<>();
    }

    @Override
    public <ENTITY> void install(TableIdentifier<ENTITY> tableIdentifier, SqlFunction<ResultSet, ENTITY> entityMapper) {
        final SqlStreamSupplier<ENTITY> supplier = new SqlStreamSupplierImpl<>(
            tableIdentifier, 
            entityMapper, 
            projectComponent, 
            dbmsHandlerComponent,
            managerComponent
        );
        
        supportMap.put(tableIdentifier, supplier);
    }

    @Override
    public <ENTITY> Stream<ENTITY> stream(TableIdentifier<ENTITY> tableIdentifier, ParallelStrategy parallelStrategy) {
        final SqlStreamSupplier<ENTITY> supplier = getStreamSupplier(tableIdentifier);
        return supplier.stream(parallelStrategy);
    }

    private <ENTITY> SqlStreamSupplier<ENTITY> getStreamSupplier(TableIdentifier<ENTITY> tableIdentifier) {
        @SuppressWarnings("unchecked")
        final SqlStreamSupplier<ENTITY> streamSupplier = (SqlStreamSupplier<ENTITY>) supportMap.get(tableIdentifier);
        return requireNonNull(streamSupplier, 
            "No Sql Stream Supplier installed for table identifier " + 
            tableIdentifier
        );
    }
}