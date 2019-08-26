/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.geode.cache.lucene.internal.distributed;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.geode.DataSerializer;
import org.apache.geode.cache.lucene.LuceneQueryFactory;
import org.apache.geode.cache.lucene.internal.repository.IndexResultCollector;
import org.apache.geode.internal.Version;
import org.apache.geode.internal.serialization.DataSerializableFixedID;
import org.apache.geode.internal.serialization.SerializationContext;

/**
 * An implementation of {@link IndexResultCollector} to collect {@link EntryScore}. It is expected
 * that the results will be ordered by score of the entry.
 */
public class TopEntriesCollector implements IndexResultCollector, DataSerializableFixedID {
  private String name;

  private TopEntries entries;

  public TopEntriesCollector() {
    this(null);
  }

  public TopEntriesCollector(String name) {
    this(name, LuceneQueryFactory.DEFAULT_LIMIT);
  }

  public TopEntriesCollector(String name, int limit) {
    this.name = name;
    this.entries = new TopEntries(limit);
  }

  @Override
  public void collect(Object key, float score) {
    collect(new EntryScore(key, score));
  }

  public void collect(EntryScore entry) {
    entries.addHit(entry);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int size() {
    TopEntries entries = getEntries();
    return entries == null ? 0 : entries.size();
  }

  /**
   * @return The entries collected by this collector
   */
  public TopEntries getEntries() {
    return entries;
  }

  @Override
  public Version[] getSerializationVersions() {
    return null;
  }

  @Override
  public int getDSFID() {
    return LUCENE_TOP_ENTRIES_COLLECTOR;
  }

  @Override
  public void toData(DataOutput out,
      SerializationContext context) throws IOException {
    DataSerializer.writeString(name, out);
    DataSerializer.writeObject(entries, out);
  }

  @Override
  public void fromData(DataInput in,
      SerializationContext context) throws IOException, ClassNotFoundException {
    name = DataSerializer.readString(in);
    entries = DataSerializer.readObject(in);
  }
}
