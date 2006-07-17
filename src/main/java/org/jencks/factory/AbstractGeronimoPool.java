/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jencks.factory;

/**
 * This abstract class defines common properties for every
 * kind of pool used by the connection manager of Geronimo.
 * <p/>
 * These properties are the following:
 * - maxSize: the max size of the pool
 * - minSize: the min size of the pool
 * - blockingTimeoutMilliseconds: the blocking timeout of the pool
 * in milliseconds
 * - idleTimeoutMinutes: the idle timeout of the pool in minutes
 * - matchOne:
 * - matchAll:
 * selectOneAssumeMatch:
 *
 * @author Thierry Templier
 * @see PartitionedPoolFactoryBean
 * @see SinglePoolFactoryBean
 */
public abstract class AbstractGeronimoPool {

    protected int maxSize;
    protected int minSize;
    protected int blockingTimeoutMilliseconds;
    protected int idleTimeoutMinutes;
    protected boolean matchOne;
    protected boolean matchAll;
    protected boolean selectOneAssumeMatch;

    public int getBlockingTimeoutMilliseconds() {
        return blockingTimeoutMilliseconds;
    }

    public int getIdleTimeoutMinutes() {
        return idleTimeoutMinutes;
    }

    public boolean isMatchAll() {
        return matchAll;
    }

    public boolean isMatchOne() {
        return matchOne;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int getMinSize() {
        return minSize;
    }

    public boolean isSelectOneAssumeMatch() {
        return selectOneAssumeMatch;
    }

    /**
     * Set the blocking timeout property in milliseconds.
     */
    public void setBlockingTimeoutMilliseconds(int blockingTimeoutMilliseconds) {
        this.blockingTimeoutMilliseconds = blockingTimeoutMilliseconds;
    }

    /**
     * Set the idle timeout property in minutes.
     */
    public void setIdleTimeoutMinutes(int idleTimeoutMinutes) {
        this.idleTimeoutMinutes = idleTimeoutMinutes;
    }

    /**
     * Set the match all property.
     */
    public void setMatchAll(boolean matchAll) {
        this.matchAll = matchAll;
    }

    /**
     * Set the match one property.
     */
    public void setMatchOne(boolean matchOne) {
        this.matchOne = matchOne;
    }

    /**
     * Set the max size property of the pool.
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * Set the min size property of the pool.
     */
    public void setMinSize(int minSize) {
        this.minSize = minSize;
    }

    /**
     * Set the select one assume mathc property.
     */
    public void setSelectOneAssumeMatch(boolean selectOneAssumeMatch) {
        this.selectOneAssumeMatch = selectOneAssumeMatch;
	}

}