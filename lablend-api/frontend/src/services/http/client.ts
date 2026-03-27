const DEFAULT_BASE_URL = '/api'

export class HttpError extends Error {
  readonly status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'HttpError'
    this.status = status
  }
}

const getBaseUrl = (): string => {
  const envBaseUrl = import.meta.env.VITE_API_BASE_URL
  if (typeof envBaseUrl === 'string' && envBaseUrl.trim().length > 0) {
    return envBaseUrl
  }
  return DEFAULT_BASE_URL
}

const makeUrl = (path: string): string => {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  return `${getBaseUrl()}${normalizedPath}`
}

const parseErrorMessage = async (response: Response): Promise<string> => {
  const contentType = response.headers.get('content-type')
  if (contentType?.includes('application/json')) {
    const payload = (await response.json()) as { message?: string }
    if (payload.message) {
      return payload.message
    }
  }

  const text = await response.text()
  if (text.trim()) {
    return text
  }

  return `Request failed with status ${response.status}`
}

export const httpClient = {
  async get<T>(path: string): Promise<T> {
    const response = await fetch(makeUrl(path))
    if (!response.ok) {
      throw new HttpError(await parseErrorMessage(response), response.status)
    }
    return (await response.json()) as T
  },

  async post<TResponse, TBody>(path: string, body: TBody): Promise<TResponse> {
    const response = await fetch(makeUrl(path), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    })

    if (!response.ok) {
      throw new HttpError(await parseErrorMessage(response), response.status)
    }

    return (await response.json()) as TResponse
  },

  async put<TResponse, TBody>(path: string, body: TBody): Promise<TResponse> {
    const response = await fetch(makeUrl(path), {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    })

    if (!response.ok) {
      throw new HttpError(await parseErrorMessage(response), response.status)
    }

    return (await response.json()) as TResponse
  },

  async delete(path: string): Promise<void> {
    const response = await fetch(makeUrl(path), {
      method: 'DELETE',
    })

    if (!response.ok) {
      throw new HttpError(await parseErrorMessage(response), response.status)
    }
  },
}
