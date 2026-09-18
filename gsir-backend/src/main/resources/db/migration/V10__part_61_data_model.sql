-- V10__part_61_data_model.sql
-- Satisfies Part 61.19 (Document Processing Record) and 61.77 (Chunk Data)

ALTER TABLE files
ADD COLUMN processing_status VARCHAR(32) NOT NULL DEFAULT 'READY';

ALTER TABLE document_chunks
ADD COLUMN embedding_reference VARCHAR(500),
ADD COLUMN embedding_model VARCHAR(255);

