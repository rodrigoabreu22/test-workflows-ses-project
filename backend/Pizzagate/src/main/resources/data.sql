INSERT INTO pizzerias (name, address, phone)
SELECT 'Napoli Centro', 'Rua das Flores 10, Porto', '+351 220 100 100'
WHERE NOT EXISTS (
  SELECT 1
  FROM pizzerias
  WHERE name = 'Napoli Centro'
);

INSERT INTO pizzerias (name, address, phone)
SELECT 'Roma Antica', 'Avenida da Liberdade 22, Lisboa', '+351 210 200 200'
WHERE NOT EXISTS (
  SELECT 1
  FROM pizzerias
  WHERE name = 'Roma Antica'
);

INSERT INTO pizzerias (name, address, phone)
SELECT 'Sicilia Slice', 'Praça do Mercado 5, Coimbra', '+351 239 300 300'
WHERE NOT EXISTS (
  SELECT 1
  FROM pizzerias
  WHERE name = 'Sicilia Slice'
);

INSERT INTO pizzas (name, ingredients, pizzeria_id, price, size, is_available, photo_url)
SELECT 'Margherita', 'Tomato, mozzarella, basil', p.id, 8.90, 'medium', TRUE, NULL
FROM pizzerias p
WHERE p.name = 'Napoli Centro'
  AND NOT EXISTS (
    SELECT 1
    FROM pizzas z
    WHERE z.name = 'Margherita'
      AND z.pizzeria_id = p.id
  );

INSERT INTO pizzas (name, ingredients, pizzeria_id, price, size, is_available, photo_url)
SELECT 'Calzone Classico', 'Ham, mozzarella, mushrooms', p.id, 10.50, 'large', TRUE, NULL
FROM pizzerias p
WHERE p.name = 'Napoli Centro'
  AND NOT EXISTS (
    SELECT 1
    FROM pizzas z
    WHERE z.name = 'Calzone Classico'
      AND z.pizzeria_id = p.id
  );

INSERT INTO pizzas (name, ingredients, pizzeria_id, price, size, is_available, photo_url)
SELECT 'Pepperoni', 'Tomato, mozzarella, pepperoni', p.id, 11.00, 'large', TRUE, 'https://images.unsplash.com/photo-1513104890138-7c749659a591'
FROM pizzerias p
WHERE p.name = 'Roma Antica'
  AND NOT EXISTS (
    SELECT 1
    FROM pizzas z
    WHERE z.name = 'Pepperoni'
      AND z.pizzeria_id = p.id
  );

INSERT INTO pizzas (name, ingredients, pizzeria_id, price, size, is_available, photo_url)
SELECT 'Quattro Formaggi', 'Mozzarella, gorgonzola, parmesan, provolone', p.id, 12.40, 'medium', TRUE, NULL
FROM pizzerias p
WHERE p.name = 'Roma Antica'
  AND NOT EXISTS (
    SELECT 1
    FROM pizzas z
    WHERE z.name = 'Quattro Formaggi'
      AND z.pizzeria_id = p.id
  );

INSERT INTO pizzas (name, ingredients, pizzeria_id, price, size, is_available, photo_url)
SELECT 'Vegetariana', 'Tomato, mozzarella, peppers, onion, olives', p.id, 9.90, 'small', TRUE, NULL
FROM pizzerias p
WHERE p.name = 'Sicilia Slice'
  AND NOT EXISTS (
    SELECT 1
    FROM pizzas z
    WHERE z.name = 'Vegetariana'
      AND z.pizzeria_id = p.id
  );

INSERT INTO pizzas (name, ingredients, pizzeria_id, price, size, is_available, photo_url)
SELECT 'Diavola', 'Tomato, mozzarella, spicy salami', p.id, 11.80, 'medium', FALSE, NULL
FROM pizzerias p
WHERE p.name = 'Sicilia Slice'
  AND NOT EXISTS (
    SELECT 1
    FROM pizzas z
    WHERE z.name = 'Diavola'
      AND z.pizzeria_id = p.id
  );
