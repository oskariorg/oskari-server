
INSERT INTO oskari_statistical_datasource(locale, config, plugin) VALUES
('{"fi":{"name":"SotkaNET"},"sv":{"name":"SotkaNET"},"en":{"name":"SotkaNET"}}', '{"url" : "http://www.sotkanet.fi/rest"}', 'SotkaNET');

INSERT INTO oskari_statistical_datasource(locale, config, plugin) VALUES
('{"fi":{"name":"KHR"},"sv":{"name":"KHR"},"en":{"name":"KHR"}}', '{"url" : "https://khr.maanmittauslaitos.fi/tilastopalvelu/rest"}', 'SotkaNET');

INSERT INTO oskari_statistical_datasource(locale, plugin) VALUES
('{"fi":{"name":"Omat indikaattorit"},"sv":{"name":"Dina indikatorer"},"en":{"name":"Your indicators"}}', 'UserStats'); 