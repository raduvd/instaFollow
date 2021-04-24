--Some GOOD to have sqls

--DELETE adprices from a date and their coresponding ads
--DELETE FROM public.adprice WHERE pageType='APARTAMENT' and addedAtDate ='2020-05-28'
--DELETE FROM public.ad WHERE adId not in (select adId from public.adPrice)