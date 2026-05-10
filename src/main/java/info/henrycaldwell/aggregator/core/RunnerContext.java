package info.henrycaldwell.aggregator.core;

import java.nio.file.Path;
import java.util.Map;

import info.henrycaldwell.aggregator.download.Downloader;
import info.henrycaldwell.aggregator.history.History;
import info.henrycaldwell.aggregator.publish.Publisher;
import info.henrycaldwell.aggregator.retrieve.Retriever;
import info.henrycaldwell.aggregator.stage.Stager;
import info.henrycaldwell.aggregator.transform.Pipeline;

/**
 * Record for capturing the configuration of a runner.
 * 
 * This record defines a contract for carrying the resolved components required
 * to execute a single run.
 */
record RunnerContext(
    String name,
    int posts,
    Path workDir,
    int preparationThreads,
    int publisherThreads,
    int failureLimit,
    Map<String, Retriever> retrievers,
    History history,
    Downloader downloader,
    Map<String, Pipeline> pipelines,
    Stager stager,
    Map<String, Publisher> publishers) {
}
