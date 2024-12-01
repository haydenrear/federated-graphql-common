package com.hayden.graphql.models.dataservice;

import com.hayden.graphql.federated.client.FederatedGraphQlClientBuilderHolder;
import com.hayden.graphql.models.federated.request.FederatedRequestData;
import com.hayden.utilitymodule.result.Result;
import org.reactivestreams.Publisher;
import org.springframework.graphql.client.ClientGraphQlResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collection;

/**
 * NOTE: most everything happens on the Gateway, the only reason you need communication between
 * the services is if there's some sort of happens before relationship or something that the service
 * needs to manage itself... and even then you could do two requests from the client probably instead.
 *
 * Sends the request to the service, then that service publishes to a topic that is
 * listened to - so then the DataFetcher publishes the data that is received - to enable
 * scalable multicast.
 * 
 * NOTE: 
 * Function calling service as an example
 * function calling service exists with mimetype text/function-call
 * Now Gateway calls function calling service, maybe text/function-call/audio, text/function-call/video!!
 * -- now function calling service translates into graphql for other services, writes to the q
 *    Now the Gateway service receives request on the q, sends to other services, receives, puts
 *    response on the q, then function calling service receives on the q, sends back response to
 *    gateway, gateway responds... this is for scalability and homogoneity
 *    
 *    Note that there exists a topic for service discovery - there exists a limit n connections per service,
 *    all other gateways can only communicate through the Gateway that has a connection to that service... but
 *    how do we get the client? Easily... the Gateway that does not have a connection has a DataFetcher that
 *    listens/writes to the Q, the Gateway that does listens to the Q, then sends to the service and writes back
 *    to the Q, for which that other Gateway listens. Maybe for some MimeType the connection gets proxied, but this is
 *    better for scalability. In other words in this P2P sort of thing with unlimited bandwidth, the extra jump isn't a big
 *    deal... I get high uptime in relation to the number of nodes and latency of discovery service - which can be in relation
 *    to incentive to service/gateway providers..
 *
 *    The writing everything to Q is not a bug, it's the DataStreamHeartbeat in disguise... laugh at you for saying it's
 *    expensive.
 *
 *    Probably the Gateway caches refs to things as small payloads and proxies connections for large files to something like
 *    S3, another service layer there, so then it's more of an activity feed.
 *
 *    So the question of how the self-organization happens is probably a ratio of Gateway to data service.
 *    The activity Q contains probably a count of these, and the everlasting game is to promote data service
 *    to Gateway and demote Gateway to data service to keep in line with the ratio, which is actually a weighted
 *    average according to latency, bandwidth of upload/download as well... Because you now basically have three components
 *    Gateway, Data Service, and Q, and they need to be in a ratio.
 *
 *    The Q then has it's own ratio of clusters and nodes, based on the number of partitions. Once the number of partitions
 *    exceeds a certain value... So this needs a loss function or a balancing function probably also and the ability to
 *    relay or mirror between clusters for bubbling and splitting, where the splitting happens to create a new node, and then
 *    once nodes exceed a certain value in cluster split a new cluster with a mirror.
 *
 *    So this will probably happen locally, it registers and acks a type of split. One of the nodes notices in it's calculation
 *    time for split, then sends split message in it's local, then wait for ack, then ack from quorum then onboards the split,
 *    so the split happens from a node in the pool, another service gets demoted, a service waiting, to keep in balance, according
 *    to the equation. For example you have a limited number of nodes and you have the optimizer take a node away, so it calculates
 *    it could use a node, then the solver decides where the node comes from the other for the self-organization... like a company saying
 *    we can use someone here, then you have HR create the job posting, then it waits until something becomes available and it
 *    applies and sees if it makes the balance better it hires the node for the position.
 *
 *    The current architecture could be mirrored in memory, along with node statistics, as a graph. So then the data constantly flowing
 *    could then lend itself to a better and better version of the architecture. Start by simply mirroring the architecture from the
 *    queue in memory, saving it as a dataset.
 */
public abstract class CdcGraphQlDataFetcher<T, U, E> extends RemoteDataFederation<ClientGraphQlResponse> {

    abstract Result<T, E> fire(FederatedGraphQlClientBuilderHolder.FederatedGraphQlClient.FederatedGraphQlRequestArgs federatedGraphQlClient,
                               FederatedRequestData data);

    abstract Publisher<U> subscribe();

    abstract Duration publisherTimeout();

    @Override
    Publisher<ClientGraphQlResponse> get(FederatedRequestData environment,
                                         FederatedGraphQlClientBuilderHolder.FederatedGraphQlClient.FederatedGraphQlRequestArgs federatedGraphQlClient) {
        var f = fire(federatedGraphQlClient, environment);
        if (f.isOk()) {
            return Flux.from(subscribe())
                    .buffer(publisherTimeout())
                    .flatMap(t -> {
                        var found = this.from(t);
                        if (found.isOk())
                            return Mono.just(found.get());

                        return Mono.empty();
                    });
        }

        return Flux.empty();
    }
}
