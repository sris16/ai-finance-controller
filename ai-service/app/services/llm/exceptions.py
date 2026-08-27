class AIServiceUnavailableError(Exception):
    """Raised when the AI service or API key is completely missing/unavailable."""
    pass

class AIProviderAuthenticationError(Exception):
    """Raised when the AI provider rejects the API key."""
    pass

class AIProviderRateLimitError(Exception):
    """Raised when the AI provider rate limits the request."""
    pass

class AIProviderTimeoutError(Exception):
    """Raised when the AI provider request times out."""
    pass

class AIProviderResponseError(Exception):
    """Raised when the AI provider returns a malformed or invalid response (5xx/unexpected JSON)."""
    pass
