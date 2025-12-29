-- 修复错题答案索引超出范围的问题
-- 将correct_answer值修正为正确的数组索引（从0开始）

-- 1. TCP和UDP的主要区别是？- 正确答案应该是"以上都正确"（索引3）
UPDATE questions SET correct_answer = 3 WHERE id = 'net_002';

-- 2. 数组和链表的主要区别是？- 正确答案应该是"以上都正确"（索引3）
UPDATE questions SET correct_answer = 3 WHERE id = 'ds_004';

-- 3. 栈的特点是？- 正确答案应该是"后进先出"（索引1）
UPDATE questions SET correct_answer = 1 WHERE id = 'ds_002';

-- 4. 下列哪种数据结构是线性结构？- 正确答案应该是"数组"（索引2）
UPDATE questions SET correct_answer = 2 WHERE id = 'ds_001';

-- 5. SQL中，哪个关键字用于去除查询结果中的重复行？- 正确答案应该是"B. DISTINCT"（索引1）
UPDATE questions SET correct_answer = 1 WHERE id = 'q5';

-- 6. 以下哪些是进程间通信的方式？（多选）- 这道题是多选题，需要特殊处理
-- 暂时跳过多选题，或者根据业务逻辑设置

-- 7. 在TCP/IP协议栈中，HTTP协议工作在哪一层？- 正确答案应该是"D. 应用层"（索引3）
UPDATE questions SET correct_answer = 3 WHERE id = 'q3';

-- 8. 以下哪种排序算法的平均时间复杂度是O(n log n)？- 正确答案应该是"C. 快速排序"（索引2）
UPDATE questions SET correct_answer = 2 WHERE id = 'q2';

-- 9. Java中，以下关于继承的说法哪个是正确的？- 正确答案应该是"B. 一个类只能继承一个父类"（索引1）
UPDATE questions SET correct_answer = 1 WHERE id = 'q1';

-- 验证修复结果
SELECT id, title, correct_answer, options 
FROM questions 
WHERE id IN ('net_002', 'ds_004', 'ds_002', 'ds_001', 'q5', 'q3', 'q2', 'q1');