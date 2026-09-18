-- PART 99: UNCERTAINTY PROPAGATION

ALTER TABLE experiment_observations
ADD COLUMN uncertainty DOUBLE PRECISION,
ADD COLUMN uncertainty_type VARCHAR(50),
ADD COLUMN method VARCHAR(255),
ADD COLUMN assumptions TEXT,
ADD COLUMN covariance_data TEXT;

ALTER TABLE experiment_results
ADD COLUMN uncertainty DOUBLE PRECISION,
ADD COLUMN uncertainty_type VARCHAR(50),
ADD COLUMN method VARCHAR(255),
ADD COLUMN assumptions TEXT,
ADD COLUMN covariance_data TEXT;
