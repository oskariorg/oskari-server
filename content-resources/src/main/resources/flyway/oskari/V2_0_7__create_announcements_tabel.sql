CREATE TABLE IF NOT EXISTS oskari_announcements (
    "id" serial NOT NULL,
    "title" VARCHAR(255) NOT NULL,
    "content" TEXT NOT NULL,
    "begin_date" DATE NOT NULL,
    "end_date" DATE NOT NULL,
    "active" boolean NOT NULL
);