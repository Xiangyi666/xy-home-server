-- 专门查看触发器函数
SELECT
    n.nspname as schema_name,
    p.proname as function_name,
    pg_get_function_arguments(p.oid) as arguments
FROM pg_proc p
         JOIN pg_namespace n ON p.pronamespace = n.oid
WHERE p.prorettype = 'pg_catalog.trigger'::pg_catalog.regtype
AND n.nspname NOT IN ('pg_catalog', 'information_schema')
ORDER BY schema_name, function_name;


-- 查看指定函数的完整定义
SELECT
    n.nspname as schema_name,
    p.proname as function_name,
    pg_get_function_arguments(p.oid) as arguments,
    pg_get_function_result(p.oid) as return_type,
    pg_get_functiondef(p.oid) as function_definition
FROM pg_proc p
         JOIN pg_namespace n ON p.pronamespace = n.oid
WHERE p.proname = 'your_function_name'  -- 替换为你的函数名
  AND n.nspname = 'your_schema_name';     -- 替换为你的schema名（通常是'public'）