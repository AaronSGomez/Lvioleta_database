-- public.cliente definition

-- Drop table

-- DROP TABLE public.cliente;

CREATE TABLE public.cliente (
                                id int4 NOT NULL,
                                nombre varchar(120) NOT NULL,
                                email varchar(200) NOT NULL,
                                CONSTRAINT cliente_email_key UNIQUE (email),
                                CONSTRAINT cliente_pkey PRIMARY KEY (id)
);


-- public.empresa_reparto definition

-- Drop table

-- DROP TABLE public.empresa_reparto;

CREATE TABLE public.empresa_reparto (
                                        id serial4 NOT NULL,
                                        nombre varchar(100) NOT NULL,
                                        telefono varchar(20) NULL,
                                        direccion varchar(255) NULL,
                                        CONSTRAINT empresa_reparto_pkey PRIMARY KEY (id)
);


-- public.producto definition

-- Drop table

-- DROP TABLE public.producto;

CREATE TABLE public.producto (
                                 id int4 NOT NULL,
                                 nombre varchar(120) NOT NULL,
                                 precio numeric(12, 2) NOT NULL,
                                 CONSTRAINT producto_pkey PRIMARY KEY (id),
                                 CONSTRAINT producto_precio_check CHECK ((precio >= (0)::numeric))
);


-- public.detalle_cliente definition

-- Drop table

-- DROP TABLE public.detalle_cliente;

CREATE TABLE public.detalle_cliente (
                                        id int4 NOT NULL,
                                        direccion varchar(200) NULL,
                                        telefono varchar(40) NOT NULL,
                                        notas varchar(200) NULL,
                                        CONSTRAINT detalle_cliente_pkey PRIMARY KEY (id),
                                        CONSTRAINT fk_det_cliente FOREIGN KEY (id) REFERENCES public.cliente(id) ON DELETE CASCADE
);


-- public.pedido definition

-- Drop table

-- DROP TABLE public.pedido;

CREATE TABLE public.pedido (
                               id int4 NOT NULL,
                               cliente_id int4 NOT NULL,
                               fecha date NOT NULL,
                               CONSTRAINT pedido_pkey PRIMARY KEY (id),
                               CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id) REFERENCES public.cliente(id) ON DELETE RESTRICT
);


-- public.repartidor definition

-- Drop table

-- DROP TABLE public.repartidor;

CREATE TABLE public.repartidor (
                                   id serial4 NOT NULL,
                                   nombre varchar(100) NOT NULL,
                                   telefono varchar(20) NULL,
                                   empresa_id int4 NULL,
                                   CONSTRAINT repartidor_pkey PRIMARY KEY (id),
                                   CONSTRAINT fk_repartidor_empresa FOREIGN KEY (empresa_id) REFERENCES public.empresa_reparto(id) ON DELETE SET NULL
);


-- public.detalle_pedido definition

-- Drop table

-- DROP TABLE public.detalle_pedido;

CREATE TABLE public.detalle_pedido (
                                       pedido_id int4 NOT NULL,
                                       producto_id int4 NOT NULL,
                                       cantidad int4 NOT NULL,
                                       precio_unit numeric(12, 2) NOT NULL,
                                       CONSTRAINT detalle_pedido_cantidad_check CHECK ((cantidad > 0)),
                                       CONSTRAINT detalle_pedido_pkey PRIMARY KEY (pedido_id, producto_id),
                                       CONSTRAINT detalle_pedido_precio_unit_check CHECK ((precio_unit >= (0)::numeric)),
                                       CONSTRAINT fk_dp_pedido FOREIGN KEY (pedido_id) REFERENCES public.pedido(id) ON DELETE CASCADE,
                                       CONSTRAINT fk_dp_producto FOREIGN KEY (producto_id) REFERENCES public.producto(id) ON DELETE RESTRICT
);


-- public.envio definition

-- Drop table

-- DROP TABLE public.envio;

CREATE TABLE public.envio (
                              id serial4 NOT NULL,
                              pedido_id int4 NOT NULL,
                              repartidor_id int4 NULL,
                              fecha_salida timestamp DEFAULT CURRENT_TIMESTAMP NULL,
                              numero_seguimiento varchar(50) NULL,
                              estado varchar(20) DEFAULT 'EN_PREPARACION'::character varying NULL,
                              nombre_empresa varchar(100) NULL,
                              nombre_repartidor varchar(100) NULL,
                              telefono_repartidor varchar(20) NULL,
                              nombre_cliente varchar(100) NULL,
                              direccion_entrega varchar(255) NULL,
                              CONSTRAINT envio_pedido_id_key UNIQUE (pedido_id),
                              CONSTRAINT envio_pkey PRIMARY KEY (id),
                              CONSTRAINT fk_envio_pedido FOREIGN KEY (pedido_id) REFERENCES public.pedido(id) ON DELETE CASCADE,
                              CONSTRAINT fk_envio_repartidor FOREIGN KEY (repartidor_id) REFERENCES public.repartidor(id)
);