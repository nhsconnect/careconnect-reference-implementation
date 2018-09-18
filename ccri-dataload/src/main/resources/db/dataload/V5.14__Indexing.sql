
CREATE INDEX CONCURRENTLY IF NOT EXISTS address_postcode_idx ON Address USING gist (postcode gist_trgm_ops);