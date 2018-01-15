-- reset sequence
SELECT setval('oskari_user_indicator_id_seq', (SELECT MAX(id) + 1 FROM oskari_user_indicator));