import { useCallback, useEffect, useRef, useState } from 'react'

export function useFetch(fetcher, deps = []) {
  const fetcherRef = useRef(fetcher)

  const [version, setVersion] = useState(0)
  const [state, setState] = useState({
    key: null,
    loading: true,
    data: null,
    error: null,
  })

  const reload = useCallback(() => setVersion((v) => v + 1), [])

  useEffect(() => {
    fetcherRef.current = fetcher
  })

  const stateKey = `${version}:${JSON.stringify(deps)}`
  if (state.key !== stateKey) {
    setState({ key: stateKey, loading: true, data: null, error: null })
  }

  useEffect(() => {
    let active = true

    fetcherRef
      .current()
      .then((data) => {
        if (active) {
          setState((prev) =>
            prev.key === stateKey
              ? { ...prev, loading: false, data, error: null }
              : prev,
          )
        }
      })
      .catch((error) => {
        if (active && error?.name !== 'AbortError') {
          setState((prev) =>
            prev.key === stateKey
              ? { ...prev, loading: false, data: null, error }
              : prev,
          )
        }
      })

    return () => {
      active = false
    }
  }, [stateKey])

  return {
    loading: state.loading,
    data: state.data,
    error: state.error,
    reload,
  }
}
