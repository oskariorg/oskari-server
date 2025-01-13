--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 4455 (class 0 OID 24546)
-- Dependencies: 229
-- Data for Name: portti_bundle; Type: TABLE DATA; Schema: public; Owner: oskari
--

COPY public.portti_bundle (id, name, config, state, startup) FROM stdin;
4	toolbar	{}	{}	\N
3	divmanazer	{}	{}	\N
5	statehandler	{}	{}	\N
13	maplegend	{}	{}	\N
7	search	{}	{}	\N
8	layerselector2	{}	{}	\N
9	layerselection2	{}	{}	\N
10	personaldata	{}	{}	\N
12	coordinatedisplay	{}	{}	\N
14	userguide	{\r\n    "tags" : "userguide",\r\n    "flyoutClazz" : "Oskari.mapframework.bundle.userguide.SimpleFlyout"\r\n}	{}	\N
6	infobox	{\r\n  "adaptable": true\r\n}	{}	\N
16	guidedtour	{}	{}	\N
17	backendstatus	{}	{}	\N
18	printout	{}	{}	\N
19	postprocessor	{}	{}	\N
28	publishedstatehandler	{}	{}	\N
26	featuredata2	{}	{}	\N
24	promote	{}	{}	\N
27	admin-layerrights	{}	{}	\N
29	publishedmyplaces2	{}	{}	\N
30	myplacesimport	{\r\n    "name": "MyPlacesImport",\r\n    "sandbox": "sandbox",\r\n    "flyoutClazz": "Oskari.mapframework.bundle.myplacesimport.Flyout"\r\n}	{}	\N
42	routingUI	{}	{}	\N
31	admin-users	{\r\n    "restUrl": "action_route=Users"\r\n}	{}	\N
43	routingService	{}	{}	\N
32	routesearch	{\r\n    "flyoutClazz": "Oskari.mapframework.bundle.routesearch.Flyout"\r\n}	{}	\N
33	rpc	{}	{}	\N
34	findbycoordinates	{}	{}	\N
36	admin-layerselector	{}	{}	\N
37	admin	{}	{}	\N
39	metadataflyout	{}	{}	\N
41	coordinatetool	{}	{}	\N
44	metrics	\N	\N	\N
45	admin-wfs-search-channel	\N	\N	\N
46	search-from-channels	\N	\N	\N
47	publisher2	\N	\N	\N
49	timeseries	\N	\N	\N
50	selected-featuredata	{}	{}	\N
51	content-editor	\N	\N	\N
61	language-selector	{}	{}	\N
62	camera-controls-3d	\N	\N	\N
64	dimension-change	{}	\N	\N
52	feedbackService	\N	\N	\N
53	system-message	\N	\N	\N
54	maprotator	\N	\N	\N
55	appsetup	\N	\N	\N
56	admin-publish-transfer	\N	\N	\N
20	statsgrid	{}	{}	\N
58	hierarchical-layerlist	\N	\N	\N
59	admin-hierarchical-layerlist	\N	\N	\N
60	myplaces3	\N	\N	\N
48	drawtools	\N	\N	\N
38	analyse	{}	{}	\N
40	metadatacatalogue	{}	{}	\N
35	heatmap	{}	{}	\N
2	mapfull	{}	{}	\N
63	time-control-3d	\N	\N	\N
65	layerlist	\N	\N	\N
66	admin-layereditor	\N	\N	\N
\.


--
-- TOC entry 4461 (class 0 OID 0)
-- Dependencies: 228
-- Name: portti_bundle_id_seq; Type: SEQUENCE SET; Schema: public; Owner: oskari
--

SELECT pg_catalog.setval('public.portti_bundle_id_seq', 67, true);


--
-- PostgreSQL database dump complete
--

