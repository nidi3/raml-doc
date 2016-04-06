The Search API is optimized to help you find the specific item you’re looking for (e.g., a specific user, a specific file in a repository, etc.).
Think of it the way you think of performing a search on Google.
It’s designed to help you find the one result you’re looking for (or maybe the few results you’re looking for).
Just like searching on Google, you sometimes want to see a few pages of search results so that you can find the item that best meets your needs.
To satisfy that need, the GitHub Search API provides **up to 1,000 results for each search.**

### Ranking search results
Unless another sort option is provided as a query parameter, results are sorted by best match, as indicated by the `score` field for each item returned.
This is a computed value representing the relevance of an item relative to the other items in the result set.
Multiple factors are combined to boost the most relevant item to the top of the result list.

### Rate limit
The Search API has a custom rate limit. For requests using [Basic Authentication](https://developer.github.com/v3/#authentication),
[OAuth](https://developer.github.com/v3/#authentication), or
[client ID and secret](https://developer.github.com/v3/#increasing-the-unauthenticated-rate-limit-for-oauth-applications),
you can make up to 30 requests per minute.
For unauthenticated requests, the rate limit allows you to make up to 10 requests per minute.

See the [rate limit documentation](https://developer.github.com/v3/#rate-limiting) for details on determining your current rate limit status.

### Timeouts and incomplete results
To keep the Search API fast for everyone, we limit how long any individual query can run.
For queries that [exceed the time limit](https://developer.github.com/changes/2014-04-07-understanding-search-results-and-potential-timeouts/),
the API returns the matches that were already found prior to the timeout, and the response has the `incomplete_results` property set to `true`.
Reaching a timeout does not necessarily mean that search results are incomplete. More results might have been found, but also might not.
