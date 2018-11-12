package cern.c2mon.server.elasticsearch.bulk;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;

/**
 * Defines an interface for communicating with Elasticsearch luster using bulk data operations.
 *
 * @author Serhiy Boychenko
 */
public interface BulkProcessorProxy {

  /**
   * Allows to perform bulk {@link IndexRequest}s.
   *
   * @param request to be executed in bulk action.
   */
  void add(IndexRequest request);

  /**
   * @return true in case bulk operation concluded successfully, false otherwise.
   */
  boolean flush();
}
