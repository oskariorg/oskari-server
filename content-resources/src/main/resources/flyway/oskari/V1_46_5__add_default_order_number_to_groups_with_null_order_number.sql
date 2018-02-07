UPDATE oskari_maplayer_group omlg
    SET order_number = omlg2.seqnum-1
    FROM (SELECT id, row_number() over (ORDER BY id) AS seqnum
          FROM oskari_maplayer_group omlg2
         ) omlg2
    WHERE omlg2.id = omlg.id AND omlg.parentid = -1