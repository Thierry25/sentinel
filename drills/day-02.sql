-- Q1
SELECT a.ACCOUNT_ID,
       COUNT(o.ORDER_ID)                                                          AS nb_of_orders,
       COUNT(CASE WHEN o.ORDER_ID IS NOT NULL AND o.LIMIT_PRICE IS NULL THEN 1 END) AS nb_of_market_orders,
       COALESCE(SUM(CASE WHEN o.LIMIT_PRICE IS NULL THEN o.QTY END), 0)           AS total_qty_market_orders
FROM ACCOUNT a
         LEFT JOIN ORDERS o ON o.ACCOUNT_ID = a.ACCOUNT_ID
GROUP BY a.ACCOUNT_ID
ORDER BY a.ACCOUNT_ID;

-- Result
-- A100	5	2	500
-- A101	4	1	50
-- A200	3	1	400
-- A300	2	1	20000
-- A400	0	0	0

-- Q2
SELECT a.ACCOUNT_ID FROM ACCOUNT a
WHERE EXISTS(
    SELECT 1 FROM ORDERS o
    WHERE o.ACCOUNT_ID = a.ACCOUNT_ID
)
  AND NOT EXISTS(
    SELECT 1 FROM ORDERS o1
    WHERE a.ACCOUNT_ID  = o1.ACCOUNT_ID
      AND o1.STATUS = 'FILLED'
);

-- Result
-- A200

-- Q3
SELECT DISTINCT o.SYMBOL FROM ORDERS o
WHERE NOT EXISTS (
    SELECT 1 FROM TRADE t
    WHERE o.SYMBOL = t.SYMBOL
);

-- Result
-- NVDA

-- Q4
-- NOT IN
SELECT a.ACCOUNT_ID
FROM ACCOUNT a
WHERE a.ACCOUNT_ID NOT IN (SELECT ACCOUNT_ID FROM WATCHLIST);
-- no rows
-- Why: WATCHLIST.ACCOUNT_ID contains a NULL. x NOT IN (a, b, NULL) means x <> a AND x <> b AND x <> NULL.
-- x <> NULL is UNKNOWN (never TRUE), so the whole condition is never TRUE → zero rows, for every account.
-- NOT EXISTS has no such problem. Rule: never use NOT IN on a nullable column.

-- Result
-- no rows
SELECT a.ACCOUNT_ID
FROM ACCOUNT a
WHERE NOT EXISTS (
    SELECT 1 FROM WATCHLIST w
    WHERE a.ACCOUNT_ID = w.ACCOUNT_ID
);
-- Result
-- A100
-- A200
-- A300
-- A400

-- Q5
SELECT a.ACCOUNT_ID
FROM ACCOUNT a
WHERE EXISTS (
    SELECT 1 FROM TRADE t
    WHERE a.ACCOUNT_ID = t.ACCOUNT_ID
      AND t.PRICE * t.QTY > 50000
)
  AND EXISTS (
    SELECT 1 FROM ORDERS o
    WHERE a.ACCOUNT_ID = o.ACCOUNT_ID
      AND o.STATUS = 'CANCELLED'
);
-- Result
-- A101
-- A200
