-- Assigns new id values for any user indicators that have a conflicting id due to previously faulty sequence reset issue.
-- This might break some embedded maps and saved appsetups if they have references to the conflicting ids.
-- But it's better the get new ids for ALL conflicts instead of showing the wrong data for the previous id
-- Non-conflicting ids are not affected
UPDATE oskari_user_indicator SET id = DEFAULT WHERE id in (
  select id
  from (select id, count(id) as count, count(distinct id) as uniqueCount FROM oskari_user_indicator group by id ) as duplicates
  where count <> uniqueCount
)