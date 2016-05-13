package eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.api.auth;

import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.api.Pair;

import java.util.Map;
import java.util.List;

public interface Authentication {
  /** Apply authentication settings to header and query params. */
  void applyToParams(List<Pair> queryParams, Map<String, String> headerParams);
}
