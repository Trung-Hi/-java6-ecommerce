USE [master]
GO
/****** Object:  Database [ASM_Java5]    Script Date: 23/04/2026 1:05:18 CH ******/
CREATE DATABASE [ASM_Java5]
 CONTAINMENT = NONE
 ON  PRIMARY
( NAME = N'ASM_Java5', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL17.MSSQLSERVER\MSSQL\DATA\ASM_Java5.mdf' , SIZE = 73728KB , MAXSIZE = UNLIMITED, FILEGROWTH = 65536KB )
 LOG ON
( NAME = N'ASM_Java5_log', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL17.MSSQLSERVER\MSSQL\DATA\ASM_Java5_log.ldf' , SIZE = 139264KB , MAXSIZE = 2048GB , FILEGROWTH = 65536KB )
 WITH CATALOG_COLLATION = DATABASE_DEFAULT, LEDGER = OFF
GO
ALTER DATABASE [ASM_Java5] SET COMPATIBILITY_LEVEL = 150
GO
IF (1 = FULLTEXTSERVICEPROPERTY('IsFullTextInstalled'))
begin
EXEC [ASM_Java5].[dbo].[sp_fulltext_database] @action = 'enable'
end
GO
ALTER DATABASE [ASM_Java5] SET ANSI_NULL_DEFAULT OFF
GO
ALTER DATABASE [ASM_Java5] SET ANSI_NULLS OFF
GO
ALTER DATABASE [ASM_Java5] SET ANSI_PADDING OFF
GO
ALTER DATABASE [ASM_Java5] SET ANSI_WARNINGS OFF
GO
ALTER DATABASE [ASM_Java5] SET ARITHABORT OFF
GO
ALTER DATABASE [ASM_Java5] SET AUTO_CLOSE OFF
GO
ALTER DATABASE [ASM_Java5] SET AUTO_SHRINK OFF
GO
ALTER DATABASE [ASM_Java5] SET AUTO_UPDATE_STATISTICS ON
GO
ALTER DATABASE [ASM_Java5] SET CURSOR_CLOSE_ON_COMMIT OFF
GO
ALTER DATABASE [ASM_Java5] SET CURSOR_DEFAULT  GLOBAL
GO
ALTER DATABASE [ASM_Java5] SET CONCAT_NULL_YIELDS_NULL OFF
GO
ALTER DATABASE [ASM_Java5] SET NUMERIC_ROUNDABORT OFF
GO
ALTER DATABASE [ASM_Java5] SET QUOTED_IDENTIFIER OFF
GO
ALTER DATABASE [ASM_Java5] SET RECURSIVE_TRIGGERS OFF
GO
ALTER DATABASE [ASM_Java5] SET  DISABLE_BROKER
GO
ALTER DATABASE [ASM_Java5] SET AUTO_UPDATE_STATISTICS_ASYNC OFF
GO
ALTER DATABASE [ASM_Java5] SET DATE_CORRELATION_OPTIMIZATION OFF
GO
ALTER DATABASE [ASM_Java5] SET TRUSTWORTHY OFF
GO
ALTER DATABASE [ASM_Java5] SET ALLOW_SNAPSHOT_ISOLATION OFF
GO
ALTER DATABASE [ASM_Java5] SET PARAMETERIZATION SIMPLE
GO
ALTER DATABASE [ASM_Java5] SET READ_COMMITTED_SNAPSHOT OFF
GO
ALTER DATABASE [ASM_Java5] SET HONOR_BROKER_PRIORITY OFF
GO
ALTER DATABASE [ASM_Java5] SET RECOVERY FULL
GO
ALTER DATABASE [ASM_Java5] SET  MULTI_USER
GO
ALTER DATABASE [ASM_Java5] SET PAGE_VERIFY CHECKSUM
GO
ALTER DATABASE [ASM_Java5] SET DB_CHAINING OFF
GO
ALTER DATABASE [ASM_Java5] SET FILESTREAM( NON_TRANSACTED_ACCESS = OFF )
GO
ALTER DATABASE [ASM_Java5] SET TARGET_RECOVERY_TIME = 60 SECONDS
GO
ALTER DATABASE [ASM_Java5] SET DELAYED_DURABILITY = DISABLED
GO
ALTER DATABASE [ASM_Java5] SET ACCELERATED_DATABASE_RECOVERY = OFF
GO
EXEC sys.sp_db_vardecimal_storage_format N'ASM_Java5', N'ON'
GO
ALTER DATABASE [ASM_Java5] SET QUERY_STORE = ON
GO
ALTER DATABASE [ASM_Java5] SET QUERY_STORE (OPERATION_MODE = READ_WRITE, CLEANUP_POLICY = (STALE_QUERY_THRESHOLD_DAYS = 30), DATA_FLUSH_INTERVAL_SECONDS = 900, INTERVAL_LENGTH_MINUTES = 60, MAX_STORAGE_SIZE_MB = 1000, QUERY_CAPTURE_MODE = AUTO, SIZE_BASED_CLEANUP_MODE = AUTO, MAX_PLANS_PER_QUERY = 200, WAIT_STATS_CAPTURE_MODE = ON)
GO
USE [ASM_Java5]
GO
/****** Object:  Schema [ecommerce_warehouse]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE SCHEMA [ecommerce_warehouse]
GO
/****** Object:  UserDefinedFunction [dbo].[GetVariantAttributeHash]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE   FUNCTION [dbo].[GetVariantAttributeHash](@variant_id BIGINT)
RETURNS VARCHAR(MAX)
AS
BEGIN
    DECLARE @hash VARCHAR(MAX);

    SELECT @hash = STRING_AGG(
        CAST(av.attribute_id AS VARCHAR) + ':' + av.value,
        ','
    ) WITHIN GROUP (ORDER BY av.attribute_id, av.value)
    FROM VariantAttribute va
    JOIN AttributeValue av ON va.attribute_value_id = av.id
    WHERE va.variant_id = @variant_id;

    RETURN @hash;
END;
GO
/****** Object:  Table [dbo].[Product]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Product](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[product_code] [varchar](50) NOT NULL,
	[name] [nvarchar](200) NOT NULL,
	[description] [nvarchar](max) NULL,
	[base_price] [decimal](18, 2) NOT NULL,
	[discount_percent] [decimal](5, 2) NULL,
	[image_url] [varchar](500) NULL,
	[category_id] [varchar](50) NULL,
	[is_active] [bit] NULL,
	[total_quantity] [int] NULL,
	[created_at] [datetime2](7) NULL,
	[updated_at] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED
(
	[product_code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Attribute]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Attribute](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[name] [varchar](50) NOT NULL,
	[display_name] [nvarchar](50) NOT NULL,
	[data_type] [varchar](20) NULL,
	[sort_order] [int] NULL,
	[is_active] [bit] NULL,
	[created_at] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED
(
	[name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[AttributeValue]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[AttributeValue](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[attribute_id] [bigint] NOT NULL,
	[value] [varchar](100) NOT NULL,
	[display_value] [nvarchar](100) NOT NULL,
	[hex_code] [varchar](7) NULL,
	[sort_order] [int] NULL,
	[is_active] [bit] NULL,
	[created_at] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_AttributeValue] UNIQUE NONCLUSTERED
(
	[attribute_id] ASC,
	[value] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[ProductVariant]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ProductVariant](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[product_id] [bigint] NOT NULL,
	[sku] [varchar](100) NOT NULL,
	[price_adjustment] [decimal](18, 2) NULL,
	[quantity] [int] NOT NULL,
	[is_active] [bit] NULL,
	[barcode] [varchar](100) NULL,
	[weight_grams] [int] NULL,
	[created_at] [datetime2](7) NULL,
	[updated_at] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED
(
	[sku] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[VariantAttribute]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[VariantAttribute](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[variant_id] [bigint] NOT NULL,
	[attribute_value_id] [bigint] NOT NULL,
	[created_at] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_Variant_AttributeValue] UNIQUE NONCLUSTERED
(
	[variant_id] ASC,
	[attribute_value_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  View [dbo].[vw_ProductVariantDetails]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- ============================================
-- 11. VIEWS
-- ============================================

-- View: All Variants with Details
CREATE   VIEW [dbo].[vw_ProductVariantDetails]
AS
SELECT
    p.id AS product_id,
    p.product_code,
    p.name AS product_name,
    p.base_price,
    p.discount_percent,
    pv.id AS variant_id,
    pv.sku,
    pv.price_adjustment,
    (p.base_price + pv.price_adjustment) AS final_price,
    pv.quantity,
    pv.is_active AS variant_active,
    STRING_AGG(a.display_name + ': ' + av.display_value, ', ') AS variant_attributes,
    p.is_active AS product_active,
    p.image_url
FROM Product p
JOIN ProductVariant pv ON p.id = pv.product_id
LEFT JOIN VariantAttribute va ON pv.id = va.variant_id
LEFT JOIN AttributeValue av ON va.attribute_value_id = av.id
LEFT JOIN Attribute a ON av.attribute_id = a.id
GROUP BY
    p.id, p.product_code, p.name, p.base_price, p.discount_percent,
    pv.id, pv.sku, pv.price_adjustment, pv.quantity, pv.is_active,
    p.is_active, p.image_url;
GO
/****** Object:  Table [dbo].[accounts]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[accounts](
	[username] [varchar](50) NOT NULL,
	[password] [varchar](255) NOT NULL,
	[fullname] [nvarchar](100) NOT NULL,
	[email] [varchar](100) NOT NULL,
	[photo] [varchar](255) NULL,
	[activated] [bit] NOT NULL,
	[is_delete] [bit] NOT NULL,
	[phone] [varchar](20) NOT NULL,
	[address] [nvarchar](255) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[username] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED
(
	[email] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[administrative_regions]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[administrative_regions](
	[id] [int] NOT NULL,
	[name] [nvarchar](255) NOT NULL,
	[name_en] [nvarchar](255) NOT NULL,
	[code_name] [nvarchar](255) NULL,
	[code_name_en] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[administrative_units]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[administrative_units](
	[id] [int] NOT NULL,
	[full_name] [nvarchar](255) NULL,
	[full_name_en] [nvarchar](255) NULL,
	[short_name] [nvarchar](255) NULL,
	[short_name_en] [nvarchar](255) NULL,
	[code_name] [nvarchar](255) NULL,
	[code_name_en] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[attribute_value]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[attribute_value](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[created_at] [datetime2](7) NULL,
	[display_name] [varchar](50) NOT NULL,
	[is_active] [bit] NULL,
	[sort_order] [int] NULL,
	[value] [varchar](50) NOT NULL,
	[attribute_id] [bigint] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UKceashidemjin0xyktleovrqbu] UNIQUE NONCLUSTERED
(
	[attribute_id] ASC,
	[value] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[attribute_values]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[attribute_values](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[created_at] [datetime2](7) NULL,
	[display_name] [varchar](50) NOT NULL,
	[is_active] [bit] NULL,
	[sort_order] [int] NULL,
	[value] [varchar](50) NOT NULL,
	[attribute_id] [bigint] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UKd022hpe1elhy2bsqdjkqfl1ej] UNIQUE NONCLUSTERED
(
	[attribute_id] ASC,
	[value] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[attributes]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[attributes](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[code] [varchar](50) NOT NULL,
	[created_at] [datetime2](7) NULL,
	[is_active] [bit] NULL,
	[name] [varchar](50) NOT NULL,
	[sort_order] [int] NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UKotf246toe34s1kuxq01e89xl1] UNIQUE NONCLUSTERED
(
	[code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[authorities]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[authorities](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[username] [varchar](50) NULL,
	[role_id] [varchar](20) NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[cart_items]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[cart_items](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[username] [varchar](50) NOT NULL,
	[product_id] [int] NOT NULL,
	[size_id] [int] NOT NULL,
	[quantity] [int] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UK5r1s6cnbcex2scee513t5klqy] UNIQUE NONCLUSTERED
(
	[username] ASC,
	[product_id] ASC,
	[size_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_cart_items] UNIQUE NONCLUSTERED
(
	[username] ASC,
	[product_id] ASC,
	[size_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[categories]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[categories](
	[id] [varchar](20) NOT NULL,
	[name] [nvarchar](100) NOT NULL,
	[parent_id] [varchar](20) NULL,
	[slug] [varchar](200) NULL,
	[is_active] [bit] NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Category]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Category](
	[id] [varchar](50) NOT NULL,
	[name] [nvarchar](100) NOT NULL,
	[description] [nvarchar](500) NULL,
	[created_at] [datetime2](7) NULL,
	[updated_at] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[chat_messages]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[chat_messages](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[product_id] [int] NOT NULL,
	[customer_id] [varchar](50) NOT NULL,
	[admin_id] [varchar](50) NULL,
	[content] [nvarchar](max) NULL,
	[media_url] [varchar](255) NULL,
	[created_at] [datetime] NOT NULL,
	[sender_role] [varchar](10) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[jwt_tokens]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[jwt_tokens](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[token_hash] [varchar](128) NOT NULL,
	[username] [varchar](50) NULL,
	[token_type] [varchar](20) NULL,
	[revoked] [bit] NOT NULL,
	[expires_at] [datetime2](7) NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED
(
	[token_hash] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[notifications]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[notifications](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[username] [varchar](50) NOT NULL,
	[order_id] [bigint] NULL,
	[title] [nvarchar](200) NOT NULL,
	[content] [nvarchar](1000) NOT NULL,
	[is_read] [bit] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[order_details]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[order_details](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[price] [decimal](12, 2) NOT NULL,
	[quantity] [int] NOT NULL,
	[order_id] [bigint] NULL,
	[product_id] [int] NULL,
	[size_id] [int] NULL,
	[size_name] [nvarchar](10) NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[order_refund_requests]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[order_refund_requests](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[order_id] [bigint] NOT NULL,
	[username] [varchar](50) NOT NULL,
	[status] [varchar](30) NOT NULL,
	[decline_reason] [nvarchar](max) NULL,
	[created_at] [datetime] NOT NULL,
	[decided_at] [datetime] NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[orders]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[orders](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[address] [nvarchar](255) NOT NULL,
	[status] [varchar](20) NULL,
	[create_date] [datetime2](7) NOT NULL,
	[username] [varchar](50) NULL,
	[delivery_lat] [float] NULL,
	[delivery_lng] [float] NULL,
	[province_code] [nvarchar](20) NULL,
	[ward_code] [nvarchar](20) NULL,
	[shipping_phone] [nvarchar](20) NULL,
	[pay_checkout_url] [nvarchar](1000) NULL,
	[pay_qr_code] [nvarchar](max) NULL,
	[pay_account_name] [nvarchar](255) NULL,
	[pay_account_number] [nvarchar](100) NULL,
	[pay_bank_bin] [nvarchar](20) NULL,
	[pay_payment_link_id] [nvarchar](255) NULL,
	[delivery_distance_m] [bigint] NULL,
	[expected_delivery_date] [date] NULL,
	[expected_delivery_label] [nvarchar](100) NULL,
	[delivered_at] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[password_reset_tokens]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[password_reset_tokens](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[email] [varchar](255) NOT NULL,
	[expiry_time] [datetime2](7) NOT NULL,
	[token] [varchar](255) NOT NULL,
	[used] [bit] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [idx_token] UNIQUE NONCLUSTERED
(
	[token] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[product_reviews]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[product_reviews](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[username] [varchar](50) NOT NULL,
	[product_id] [int] NOT NULL,
	[order_id] [bigint] NOT NULL,
	[star_rating] [int] NOT NULL,
	[review_content] [nvarchar](2000) NULL,
	[images] [nvarchar](2000) NULL,
	[created_at] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_product_reviews] UNIQUE NONCLUSTERED
(
	[username] ASC,
	[product_id] ASC,
	[order_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[product_sizes]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[product_sizes](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[product_id] [int] NOT NULL,
	[size_id] [int] NOT NULL,
	[quantity] [int] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_product_sizes] UNIQUE NONCLUSTERED
(
	[product_id] ASC,
	[size_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[product_variant]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[product_variant](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[barcode] [varchar](100) NULL,
	[created_at] [datetime2](7) NULL,
	[is_active] [bit] NULL,
	[price_adjustment] [numeric](18, 2) NULL,
	[quantity] [int] NOT NULL,
	[sku] [varchar](100) NOT NULL,
	[updated_at] [datetime2](7) NULL,
	[product_id] [bigint] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UKivtjmjnhkb977nvkx92oyujw8] UNIQUE NONCLUSTERED
(
	[sku] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[product_variants]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[product_variants](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[product_id] [int] NOT NULL,
	[quantity] [int] NOT NULL,
	[price_adjustment] [decimal](12, 2) NULL,
	[sku] [varchar](100) NULL,
	[attributes] [varchar](500) NULL,
	[create_date] [datetime2](7) NULL,
	[created_at] [datetime2](7) NULL,
	[is_active] [bit] NULL,
	[price] [numeric](18, 2) NULL,
	[updated_at] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[products]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[products](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[name] [nvarchar](200) NOT NULL,
	[image] [varchar](255) NULL,
	[price] [decimal](12, 2) NOT NULL,
	[discount] [decimal](5, 2) NULL,
	[available] [bit] NOT NULL,
	[quantity] [int] NULL,
	[description] [nvarchar](2000) NULL,
	[create_date] [datetime2](7) NOT NULL,
	[category_id] [varchar](20) NOT NULL,
	[is_delete] [bit] NOT NULL,
	[base_price] [numeric](18, 2) NULL,
	[code] [varchar](50) NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[provinces]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[provinces](
	[code] [nvarchar](20) NOT NULL,
	[name] [nvarchar](255) NOT NULL,
	[name_en] [nvarchar](255) NULL,
	[full_name] [nvarchar](255) NOT NULL,
	[full_name_en] [nvarchar](255) NULL,
	[code_name] [nvarchar](255) NULL,
	[administrative_unit_id] [int] NULL,
PRIMARY KEY CLUSTERED
(
	[code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[roles]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[roles](
	[id] [varchar](20) NOT NULL,
	[name] [nvarchar](50) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[sizes]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[sizes](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[name] [nvarchar](10) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED
(
	[name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[variant_attribute]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[variant_attribute](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[created_at] [datetime2](7) NULL,
	[attribute_value_id] [bigint] NOT NULL,
	[variant_id] [bigint] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UKf1u0wtcc75ijirkxial6h07c6] UNIQUE NONCLUSTERED
(
	[variant_id] ASC,
	[attribute_value_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[variant_attributes]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[variant_attributes](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[created_at] [datetime2](7) NULL,
	[attribute_value_id] [bigint] NOT NULL,
	[variant_id] [bigint] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UKo2aec309c3cvs4bibwnbcw2gd] UNIQUE NONCLUSTERED
(
	[variant_id] ASC,
	[attribute_value_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[wards]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[wards](
	[code] [nvarchar](20) NOT NULL,
	[name] [nvarchar](255) NOT NULL,
	[name_en] [nvarchar](255) NULL,
	[full_name] [nvarchar](255) NULL,
	[full_name_en] [nvarchar](255) NULL,
	[code_name] [nvarchar](255) NULL,
	[province_code] [nvarchar](20) NULL,
	[administrative_unit_id] [int] NULL,
PRIMARY KEY CLUSTERED
(
	[code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [ecommerce_warehouse].[dim_category]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [ecommerce_warehouse].[dim_category](
	[category_key] [int] IDENTITY(1,1) NOT NULL,
	[category_id] [nvarchar](20) NOT NULL,
	[name] [nvarchar](100) NOT NULL,
	[parent_id] [nvarchar](20) NULL,
	[is_active] [bit] NULL,
	[created_at] [datetime] NULL,
PRIMARY KEY CLUSTERED
(
	[category_key] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_dim_category] UNIQUE NONCLUSTERED
(
	[category_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [ecommerce_warehouse].[dim_customer]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [ecommerce_warehouse].[dim_customer](
	[customer_key] [int] IDENTITY(1,1) NOT NULL,
	[username] [nvarchar](50) NOT NULL,
	[fullname] [nvarchar](100) NULL,
	[email] [nvarchar](100) NULL,
	[phone] [nvarchar](20) NULL,
	[address] [nvarchar](255) NULL,
	[join_date] [date] NULL,
	[join_year] [int] NULL,
	[join_month] [int] NULL,
	[is_active] [bit] NULL,
	[customer_segment] [nvarchar](50) NULL,
	[total_orders] [int] NULL,
	[total_spent] [decimal](18, 2) NULL,
	[created_at] [datetime] NULL,
	[updated_at] [datetime] NULL,
PRIMARY KEY CLUSTERED
(
	[customer_key] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_dim_customer] UNIQUE NONCLUSTERED
(
	[username] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [ecommerce_warehouse].[dim_date]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [ecommerce_warehouse].[dim_date](
	[date_key] [int] NOT NULL,
	[full_date] [date] NOT NULL,
	[day] [int] NOT NULL,
	[month] [int] NOT NULL,
	[year] [int] NOT NULL,
	[quarter] [int] NOT NULL,
	[weekday_name] [nvarchar](20) NOT NULL,
	[is_weekend] [bit] NOT NULL,
	[is_holiday] [bit] NULL,
	[week_of_year] [int] NULL,
PRIMARY KEY CLUSTERED
(
	[date_key] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_dim_date] UNIQUE NONCLUSTERED
(
	[full_date] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [ecommerce_warehouse].[dim_product]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [ecommerce_warehouse].[dim_product](
	[product_key] [int] IDENTITY(1,1) NOT NULL,
	[product_id] [int] NOT NULL,
	[name] [nvarchar](200) NOT NULL,
	[image] [nvarchar](255) NULL,
	[category_id] [nvarchar](20) NULL,
	[category_name] [nvarchar](100) NULL,
	[price] [decimal](12, 2) NULL,
	[discount] [decimal](5, 2) NULL,
	[stock_quantity] [int] NULL,
	[is_active] [bit] NULL,
	[created_at] [datetime] NULL,
	[updated_at] [datetime] NULL,
PRIMARY KEY CLUSTERED
(
	[product_key] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_dim_product] UNIQUE NONCLUSTERED
(
	[product_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [ecommerce_warehouse].[fact_orders]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [ecommerce_warehouse].[fact_orders](
	[order_id] [bigint] NOT NULL,
	[order_detail_id] [bigint] NOT NULL,
	[product_key] [int] NOT NULL,
	[customer_key] [int] NOT NULL,
	[date_key] [int] NOT NULL,
	[size_id] [int] NULL,
	[quantity] [int] NOT NULL,
	[unit_price] [decimal](12, 2) NOT NULL,
	[revenue] [decimal](12, 2) NOT NULL,
	[status] [nvarchar](20) NULL,
	[phone] [nvarchar](20) NULL,
	[address] [nvarchar](255) NULL,
	[order_date] [date] NOT NULL,
	[order_time] [time](7) NULL,
PRIMARY KEY CLUSTERED
(
	[order_detail_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_Attribute_Name]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE NONCLUSTERED INDEX [IX_Attribute_Name] ON [dbo].[Attribute]
(
	[name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_AttributeValue_Attribute]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE NONCLUSTERED INDEX [IX_AttributeValue_Attribute] ON [dbo].[AttributeValue]
(
	[attribute_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_AttributeValue_Value]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE NONCLUSTERED INDEX [IX_AttributeValue_Value] ON [dbo].[AttributeValue]
(
	[value] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_chat_messages_customer_id]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE NONCLUSTERED INDEX [IX_chat_messages_customer_id] ON [dbo].[chat_messages]
(
	[customer_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_chat_messages_product_id]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE NONCLUSTERED INDEX [IX_chat_messages_product_id] ON [dbo].[chat_messages]
(
	[product_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_jwt_tokens_expires_at]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE NONCLUSTERED INDEX [IX_jwt_tokens_expires_at] ON [dbo].[jwt_tokens]
(
	[expires_at] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_jwt_tokens_username_type]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE NONCLUSTERED INDEX [IX_jwt_tokens_username_type] ON [dbo].[jwt_tokens]
(
	[username] ASC,
	[token_type] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [idx_refund_order_unique]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE UNIQUE NONCLUSTERED INDEX [idx_refund_order_unique] ON [dbo].[order_refund_requests]
(
	[order_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [idx_refund_status]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE NONCLUSTERED INDEX [idx_refund_status] ON [dbo].[order_refund_requests]
(
	[status] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [idx_refund_username]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE NONCLUSTERED INDEX [idx_refund_username] ON [dbo].[order_refund_requests]
(
	[username] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [idx_orders_province_code]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE NONCLUSTERED INDEX [idx_orders_province_code] ON [dbo].[orders]
(
	[province_code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [idx_orders_ward_code]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE NONCLUSTERED INDEX [idx_orders_ward_code] ON [dbo].[orders]
(
	[ward_code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [idx_email]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE NONCLUSTERED INDEX [idx_email] ON [dbo].[password_reset_tokens]
(
	[email] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_Product_Active]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE NONCLUSTERED INDEX [IX_Product_Active] ON [dbo].[Product]
(
	[is_active] ASC
)
WHERE ([is_active]=(1))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_Product_Category]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE NONCLUSTERED INDEX [IX_Product_Category] ON [dbo].[Product]
(
	[category_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_Product_Code]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE NONCLUSTERED INDEX [IX_Product_Code] ON [dbo].[Product]
(
	[product_code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UKhcpr86kgtroqvxu1mxoyx4ahm]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE UNIQUE NONCLUSTERED INDEX [UKhcpr86kgtroqvxu1mxoyx4ahm] ON [dbo].[Product]
(
	[product_code] ASC
)
WHERE ([product_code] IS NOT NULL)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_product_variants_product_id]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE NONCLUSTERED INDEX [IX_product_variants_product_id] ON [dbo].[product_variants]
(
	[product_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_product_variants_sku]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE NONCLUSTERED INDEX [IX_product_variants_sku] ON [dbo].[product_variants]
(
	[sku] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UK57ivhy5aj3qfmdvl6vxdfjs4p]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE UNIQUE NONCLUSTERED INDEX [UK57ivhy5aj3qfmdvl6vxdfjs4p] ON [dbo].[products]
(
	[code] ASC
)
WHERE ([code] IS NOT NULL)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_Variant_Active]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE NONCLUSTERED INDEX [IX_Variant_Active] ON [dbo].[ProductVariant]
(
	[is_active] ASC
)
WHERE ([is_active]=(1))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_Variant_Product]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE NONCLUSTERED INDEX [IX_Variant_Product] ON [dbo].[ProductVariant]
(
	[product_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_Variant_SKU]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE NONCLUSTERED INDEX [IX_Variant_SKU] ON [dbo].[ProductVariant]
(
	[sku] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [idx_provinces_unit]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE NONCLUSTERED INDEX [idx_provinces_unit] ON [dbo].[provinces]
(
	[administrative_unit_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_VA_AttributeValue]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE NONCLUSTERED INDEX [IX_VA_AttributeValue] ON [dbo].[VariantAttribute]
(
	[attribute_value_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_VA_Variant]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE NONCLUSTERED INDEX [IX_VA_Variant] ON [dbo].[VariantAttribute]
(
	[variant_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [idx_wards_province]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE NONCLUSTERED INDEX [idx_wards_province] ON [dbo].[wards]
(
	[province_code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [idx_wards_unit]    Script Date: 23/04/2026 1:05:19 CH ******/
CREATE NONCLUSTERED INDEX [idx_wards_unit] ON [dbo].[wards]
(
	[administrative_unit_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
ALTER TABLE [dbo].[accounts] ADD  CONSTRAINT [DF_accounts_activated]  DEFAULT ((1)) FOR [activated]
GO
ALTER TABLE [dbo].[accounts] ADD  CONSTRAINT [DF_accounts_is_delete]  DEFAULT ((0)) FOR [is_delete]
GO
ALTER TABLE [dbo].[Attribute] ADD  DEFAULT ('string') FOR [data_type]
GO
ALTER TABLE [dbo].[Attribute] ADD  DEFAULT ((0)) FOR [sort_order]
GO
ALTER TABLE [dbo].[Attribute] ADD  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[Attribute] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[AttributeValue] ADD  DEFAULT ((0)) FOR [sort_order]
GO
ALTER TABLE [dbo].[AttributeValue] ADD  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[AttributeValue] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[cart_items] ADD  CONSTRAINT [DF_cart_items_quantity]  DEFAULT ((1)) FOR [quantity]
GO
ALTER TABLE [dbo].[cart_items] ADD  CONSTRAINT [DF_cart_items_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[Category] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[Category] ADD  DEFAULT (getdate()) FOR [updated_at]
GO
ALTER TABLE [dbo].[chat_messages] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[chat_messages] ADD  DEFAULT ('USER') FOR [sender_role]
GO
ALTER TABLE [dbo].[jwt_tokens] ADD  CONSTRAINT [DF_jwt_tokens_revoked]  DEFAULT ((0)) FOR [revoked]
GO
ALTER TABLE [dbo].[jwt_tokens] ADD  CONSTRAINT [DF_jwt_tokens_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[notifications] ADD  CONSTRAINT [DF_notifications_is_read]  DEFAULT ((0)) FOR [is_read]
GO
ALTER TABLE [dbo].[notifications] ADD  CONSTRAINT [DF_notifications_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[order_refund_requests] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[orders] ADD  CONSTRAINT [DF_orders_create_date]  DEFAULT (sysdatetime()) FOR [create_date]
GO
ALTER TABLE [dbo].[Product] ADD  DEFAULT ((0)) FOR [base_price]
GO
ALTER TABLE [dbo].[Product] ADD  DEFAULT ((0)) FOR [discount_percent]
GO
ALTER TABLE [dbo].[Product] ADD  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[Product] ADD  DEFAULT ((0)) FOR [total_quantity]
GO
ALTER TABLE [dbo].[Product] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[Product] ADD  DEFAULT (getdate()) FOR [updated_at]
GO
ALTER TABLE [dbo].[product_reviews] ADD  CONSTRAINT [DF_product_reviews_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[product_variants] ADD  DEFAULT ((0)) FOR [quantity]
GO
ALTER TABLE [dbo].[product_variants] ADD  DEFAULT ((0.00)) FOR [price_adjustment]
GO
ALTER TABLE [dbo].[product_variants] ADD  DEFAULT (getdate()) FOR [create_date]
GO
ALTER TABLE [dbo].[products] ADD  CONSTRAINT [DF_products_available]  DEFAULT ((1)) FOR [available]
GO
ALTER TABLE [dbo].[products] ADD  CONSTRAINT [DF_products_create_date]  DEFAULT (sysdatetime()) FOR [create_date]
GO
ALTER TABLE [dbo].[products] ADD  CONSTRAINT [DF_products_is_delete]  DEFAULT ((0)) FOR [is_delete]
GO
ALTER TABLE [dbo].[ProductVariant] ADD  DEFAULT ((0)) FOR [price_adjustment]
GO
ALTER TABLE [dbo].[ProductVariant] ADD  DEFAULT ((0)) FOR [quantity]
GO
ALTER TABLE [dbo].[ProductVariant] ADD  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[ProductVariant] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[ProductVariant] ADD  DEFAULT (getdate()) FOR [updated_at]
GO
ALTER TABLE [dbo].[VariantAttribute] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [ecommerce_warehouse].[dim_category] ADD  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [ecommerce_warehouse].[dim_category] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [ecommerce_warehouse].[dim_customer] ADD  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [ecommerce_warehouse].[dim_customer] ADD  DEFAULT ((0)) FOR [total_orders]
GO
ALTER TABLE [ecommerce_warehouse].[dim_customer] ADD  DEFAULT ((0)) FOR [total_spent]
GO
ALTER TABLE [ecommerce_warehouse].[dim_customer] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [ecommerce_warehouse].[dim_customer] ADD  DEFAULT (getdate()) FOR [updated_at]
GO
ALTER TABLE [ecommerce_warehouse].[dim_date] ADD  DEFAULT ((0)) FOR [is_holiday]
GO
ALTER TABLE [ecommerce_warehouse].[dim_product] ADD  DEFAULT ((0)) FOR [stock_quantity]
GO
ALTER TABLE [ecommerce_warehouse].[dim_product] ADD  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [ecommerce_warehouse].[dim_product] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [ecommerce_warehouse].[dim_product] ADD  DEFAULT (getdate()) FOR [updated_at]
GO
ALTER TABLE [ecommerce_warehouse].[fact_orders] ADD  DEFAULT ((1)) FOR [quantity]
GO
ALTER TABLE [dbo].[attribute_value]  WITH CHECK ADD  CONSTRAINT [FK59xqw12tl928rqcdu2h9o6mau] FOREIGN KEY([attribute_id])
REFERENCES [dbo].[Attribute] ([id])
GO
ALTER TABLE [dbo].[attribute_value] CHECK CONSTRAINT [FK59xqw12tl928rqcdu2h9o6mau]
GO
ALTER TABLE [dbo].[attribute_values]  WITH CHECK ADD  CONSTRAINT [FKkaq0fvnivyrmqu857uy04xgmm] FOREIGN KEY([attribute_id])
REFERENCES [dbo].[attributes] ([id])
GO
ALTER TABLE [dbo].[attribute_values] CHECK CONSTRAINT [FKkaq0fvnivyrmqu857uy04xgmm]
GO
ALTER TABLE [dbo].[AttributeValue]  WITH CHECK ADD  CONSTRAINT [FK_AttributeValue_Attribute] FOREIGN KEY([attribute_id])
REFERENCES [dbo].[Attribute] ([id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[AttributeValue] CHECK CONSTRAINT [FK_AttributeValue_Attribute]
GO
ALTER TABLE [dbo].[authorities]  WITH CHECK ADD  CONSTRAINT [FK_authorities_accounts] FOREIGN KEY([username])
REFERENCES [dbo].[accounts] ([username])
GO
ALTER TABLE [dbo].[authorities] CHECK CONSTRAINT [FK_authorities_accounts]
GO
ALTER TABLE [dbo].[authorities]  WITH CHECK ADD  CONSTRAINT [FK_authorities_roles] FOREIGN KEY([role_id])
REFERENCES [dbo].[roles] ([id])
GO
ALTER TABLE [dbo].[authorities] CHECK CONSTRAINT [FK_authorities_roles]
GO
ALTER TABLE [dbo].[cart_items]  WITH CHECK ADD  CONSTRAINT [FK_cart_items_accounts] FOREIGN KEY([username])
REFERENCES [dbo].[accounts] ([username])
GO
ALTER TABLE [dbo].[cart_items] CHECK CONSTRAINT [FK_cart_items_accounts]
GO
ALTER TABLE [dbo].[cart_items]  WITH CHECK ADD  CONSTRAINT [FK_cart_items_products] FOREIGN KEY([product_id])
REFERENCES [dbo].[products] ([id])
GO
ALTER TABLE [dbo].[cart_items] CHECK CONSTRAINT [FK_cart_items_products]
GO
ALTER TABLE [dbo].[cart_items]  WITH CHECK ADD  CONSTRAINT [FK_cart_items_sizes] FOREIGN KEY([size_id])
REFERENCES [dbo].[sizes] ([id])
GO
ALTER TABLE [dbo].[cart_items] CHECK CONSTRAINT [FK_cart_items_sizes]
GO
ALTER TABLE [dbo].[chat_messages]  WITH CHECK ADD  CONSTRAINT [FK_chat_messages_admin] FOREIGN KEY([admin_id])
REFERENCES [dbo].[accounts] ([username])
GO
ALTER TABLE [dbo].[chat_messages] CHECK CONSTRAINT [FK_chat_messages_admin]
GO
ALTER TABLE [dbo].[chat_messages]  WITH CHECK ADD  CONSTRAINT [FK_chat_messages_customer] FOREIGN KEY([customer_id])
REFERENCES [dbo].[accounts] ([username])
GO
ALTER TABLE [dbo].[chat_messages] CHECK CONSTRAINT [FK_chat_messages_customer]
GO
ALTER TABLE [dbo].[chat_messages]  WITH CHECK ADD  CONSTRAINT [FK_chat_messages_products] FOREIGN KEY([product_id])
REFERENCES [dbo].[products] ([id])
GO
ALTER TABLE [dbo].[chat_messages] CHECK CONSTRAINT [FK_chat_messages_products]
GO
ALTER TABLE [dbo].[notifications]  WITH CHECK ADD  CONSTRAINT [FK_notifications_accounts] FOREIGN KEY([username])
REFERENCES [dbo].[accounts] ([username])
GO
ALTER TABLE [dbo].[notifications] CHECK CONSTRAINT [FK_notifications_accounts]
GO
ALTER TABLE [dbo].[notifications]  WITH CHECK ADD  CONSTRAINT [FK_notifications_orders] FOREIGN KEY([order_id])
REFERENCES [dbo].[orders] ([id])
GO
ALTER TABLE [dbo].[notifications] CHECK CONSTRAINT [FK_notifications_orders]
GO
ALTER TABLE [dbo].[order_details]  WITH CHECK ADD  CONSTRAINT [FK_order_details_orders] FOREIGN KEY([order_id])
REFERENCES [dbo].[orders] ([id])
GO
ALTER TABLE [dbo].[order_details] CHECK CONSTRAINT [FK_order_details_orders]
GO
ALTER TABLE [dbo].[order_details]  WITH CHECK ADD  CONSTRAINT [FK_order_details_products] FOREIGN KEY([product_id])
REFERENCES [dbo].[products] ([id])
GO
ALTER TABLE [dbo].[order_details] CHECK CONSTRAINT [FK_order_details_products]
GO
ALTER TABLE [dbo].[orders]  WITH CHECK ADD  CONSTRAINT [FK_orders_accounts] FOREIGN KEY([username])
REFERENCES [dbo].[accounts] ([username])
GO
ALTER TABLE [dbo].[orders] CHECK CONSTRAINT [FK_orders_accounts]
GO
ALTER TABLE [dbo].[orders]  WITH CHECK ADD  CONSTRAINT [orders_province_code_fkey] FOREIGN KEY([province_code])
REFERENCES [dbo].[provinces] ([code])
GO
ALTER TABLE [dbo].[orders] CHECK CONSTRAINT [orders_province_code_fkey]
GO
ALTER TABLE [dbo].[orders]  WITH CHECK ADD  CONSTRAINT [orders_ward_code_fkey] FOREIGN KEY([ward_code])
REFERENCES [dbo].[wards] ([code])
GO
ALTER TABLE [dbo].[orders] CHECK CONSTRAINT [orders_ward_code_fkey]
GO
ALTER TABLE [dbo].[Product]  WITH CHECK ADD  CONSTRAINT [FK_Product_Category] FOREIGN KEY([category_id])
REFERENCES [dbo].[Category] ([id])
ON DELETE SET NULL
GO
ALTER TABLE [dbo].[Product] CHECK CONSTRAINT [FK_Product_Category]
GO
ALTER TABLE [dbo].[product_reviews]  WITH CHECK ADD  CONSTRAINT [FK_product_reviews_accounts] FOREIGN KEY([username])
REFERENCES [dbo].[accounts] ([username])
GO
ALTER TABLE [dbo].[product_reviews] CHECK CONSTRAINT [FK_product_reviews_accounts]
GO
ALTER TABLE [dbo].[product_reviews]  WITH CHECK ADD  CONSTRAINT [FK_product_reviews_orders] FOREIGN KEY([order_id])
REFERENCES [dbo].[orders] ([id])
GO
ALTER TABLE [dbo].[product_reviews] CHECK CONSTRAINT [FK_product_reviews_orders]
GO
ALTER TABLE [dbo].[product_reviews]  WITH CHECK ADD  CONSTRAINT [FK_product_reviews_products] FOREIGN KEY([product_id])
REFERENCES [dbo].[products] ([id])
GO
ALTER TABLE [dbo].[product_reviews] CHECK CONSTRAINT [FK_product_reviews_products]
GO
ALTER TABLE [dbo].[product_sizes]  WITH CHECK ADD  CONSTRAINT [FK_product_sizes_products] FOREIGN KEY([product_id])
REFERENCES [dbo].[products] ([id])
GO
ALTER TABLE [dbo].[product_sizes] CHECK CONSTRAINT [FK_product_sizes_products]
GO
ALTER TABLE [dbo].[product_sizes]  WITH CHECK ADD  CONSTRAINT [FK_product_sizes_sizes] FOREIGN KEY([size_id])
REFERENCES [dbo].[sizes] ([id])
GO
ALTER TABLE [dbo].[product_sizes] CHECK CONSTRAINT [FK_product_sizes_sizes]
GO
ALTER TABLE [dbo].[product_variant]  WITH CHECK ADD  CONSTRAINT [FKgrbbs9t374m9gg43l6tq1xwdj] FOREIGN KEY([product_id])
REFERENCES [dbo].[Product] ([id])
GO
ALTER TABLE [dbo].[product_variant] CHECK CONSTRAINT [FKgrbbs9t374m9gg43l6tq1xwdj]
GO
ALTER TABLE [dbo].[product_variants]  WITH CHECK ADD  CONSTRAINT [FK_product_variants_product] FOREIGN KEY([product_id])
REFERENCES [dbo].[products] ([id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[product_variants] CHECK CONSTRAINT [FK_product_variants_product]
GO
ALTER TABLE [dbo].[products]  WITH CHECK ADD  CONSTRAINT [FK_products_categories] FOREIGN KEY([category_id])
REFERENCES [dbo].[categories] ([id])
GO
ALTER TABLE [dbo].[products] CHECK CONSTRAINT [FK_products_categories]
GO
ALTER TABLE [dbo].[ProductVariant]  WITH CHECK ADD  CONSTRAINT [FK_Variant_Product] FOREIGN KEY([product_id])
REFERENCES [dbo].[Product] ([id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[ProductVariant] CHECK CONSTRAINT [FK_Variant_Product]
GO
ALTER TABLE [dbo].[provinces]  WITH CHECK ADD  CONSTRAINT [provinces_administrative_unit_id_fkey] FOREIGN KEY([administrative_unit_id])
REFERENCES [dbo].[administrative_units] ([id])
GO
ALTER TABLE [dbo].[provinces] CHECK CONSTRAINT [provinces_administrative_unit_id_fkey]
GO
ALTER TABLE [dbo].[variant_attribute]  WITH CHECK ADD  CONSTRAINT [FK87xhxywaoh6x50dwq2htmcck6] FOREIGN KEY([attribute_value_id])
REFERENCES [dbo].[attribute_value] ([id])
GO
ALTER TABLE [dbo].[variant_attribute] CHECK CONSTRAINT [FK87xhxywaoh6x50dwq2htmcck6]
GO
ALTER TABLE [dbo].[variant_attribute]  WITH CHECK ADD  CONSTRAINT [FKftpvthf91cl81y9yu9s365ies] FOREIGN KEY([variant_id])
REFERENCES [dbo].[product_variant] ([id])
GO
ALTER TABLE [dbo].[variant_attribute] CHECK CONSTRAINT [FKftpvthf91cl81y9yu9s365ies]
GO
ALTER TABLE [dbo].[variant_attributes]  WITH CHECK ADD  CONSTRAINT [FKm60qydu9tsmq9d7qycr6s0po8] FOREIGN KEY([attribute_value_id])
REFERENCES [dbo].[attribute_values] ([id])
GO
ALTER TABLE [dbo].[variant_attributes] CHECK CONSTRAINT [FKm60qydu9tsmq9d7qycr6s0po8]
GO
ALTER TABLE [dbo].[VariantAttribute]  WITH CHECK ADD  CONSTRAINT [FK_VA_AttributeValue] FOREIGN KEY([attribute_value_id])
REFERENCES [dbo].[AttributeValue] ([id])
GO
ALTER TABLE [dbo].[VariantAttribute] CHECK CONSTRAINT [FK_VA_AttributeValue]
GO
ALTER TABLE [dbo].[VariantAttribute]  WITH CHECK ADD  CONSTRAINT [FK_VA_Variant] FOREIGN KEY([variant_id])
REFERENCES [dbo].[ProductVariant] ([id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[VariantAttribute] CHECK CONSTRAINT [FK_VA_Variant]
GO
ALTER TABLE [dbo].[wards]  WITH CHECK ADD  CONSTRAINT [wards_administrative_unit_id_fkey] FOREIGN KEY([administrative_unit_id])
REFERENCES [dbo].[administrative_units] ([id])
GO
ALTER TABLE [dbo].[wards] CHECK CONSTRAINT [wards_administrative_unit_id_fkey]
GO
ALTER TABLE [dbo].[wards]  WITH CHECK ADD  CONSTRAINT [wards_province_code_fkey] FOREIGN KEY([province_code])
REFERENCES [dbo].[provinces] ([code])
GO
ALTER TABLE [dbo].[wards] CHECK CONSTRAINT [wards_province_code_fkey]
GO
ALTER TABLE [ecommerce_warehouse].[fact_orders]  WITH CHECK ADD  CONSTRAINT [FK_fact_orders_customer] FOREIGN KEY([customer_key])
REFERENCES [ecommerce_warehouse].[dim_customer] ([customer_key])
GO
ALTER TABLE [ecommerce_warehouse].[fact_orders] CHECK CONSTRAINT [FK_fact_orders_customer]
GO
ALTER TABLE [ecommerce_warehouse].[fact_orders]  WITH CHECK ADD  CONSTRAINT [FK_fact_orders_date] FOREIGN KEY([date_key])
REFERENCES [ecommerce_warehouse].[dim_date] ([date_key])
GO
ALTER TABLE [ecommerce_warehouse].[fact_orders] CHECK CONSTRAINT [FK_fact_orders_date]
GO
ALTER TABLE [ecommerce_warehouse].[fact_orders]  WITH CHECK ADD  CONSTRAINT [FK_fact_orders_product] FOREIGN KEY([product_key])
REFERENCES [ecommerce_warehouse].[dim_product] ([product_key])
GO
ALTER TABLE [ecommerce_warehouse].[fact_orders] CHECK CONSTRAINT [FK_fact_orders_product]
GO
ALTER TABLE [dbo].[chat_messages]  WITH CHECK ADD  CONSTRAINT [CK_chat_messages_sender_role] CHECK  (([sender_role]='ADMIN' OR [sender_role]='USER'))
GO
ALTER TABLE [dbo].[chat_messages] CHECK CONSTRAINT [CK_chat_messages_sender_role]
GO
ALTER TABLE [dbo].[Product]  WITH CHECK ADD CHECK  (([discount_percent]>=(0) AND [discount_percent]<=(100)))
GO
ALTER TABLE [dbo].[ProductVariant]  WITH CHECK ADD CHECK  (([quantity]>=(0)))
GO
/****** Object:  StoredProcedure [dbo].[sp_CreateProductWithVariants]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- ============================================
-- 10. STORED PROCEDURES
-- ============================================

-- SP: Create Product with Variants
CREATE   PROCEDURE [dbo].[sp_CreateProductWithVariants]
    @product_code VARCHAR(50),
    @name NVARCHAR(200),
    @description NVARCHAR(MAX),
    @base_price DECIMAL(18,2),
    @discount_percent DECIMAL(5,2),
    @image_url VARCHAR(500),
    @category_id VARCHAR(50),
    @variants_json NVARCHAR(MAX),  -- JSON array of variants
    @new_product_id BIGINT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;

    BEGIN TRY
        -- B1: Insert Product
        INSERT INTO Product (
            product_code, name, description, base_price,
            discount_percent, image_url, category_id
        )
        VALUES (
            @product_code, @name, @description, @base_price,
            @discount_percent, @image_url, @category_id
        );

        SET @new_product_id = SCOPE_IDENTITY();

        -- B2: Parse and process variants
        DECLARE @variant_idx INT = 0;
        DECLARE @variant_count INT = JSON_VALUE(@variants_json, '$.length');

        WHILE @variant_idx < @variant_count
        BEGIN
            DECLARE @variant_path VARCHAR(100) = '$[' + CAST(@variant_idx AS VARCHAR) + ']';
            DECLARE @quantity INT = JSON_VALUE(@variants_json, @variant_path + '.quantity');
            DECLARE @price_adjustment DECIMAL(18,2) = ISNULL(JSON_VALUE(@variants_json, @variant_path + '.price_adjustment'), 0);
            DECLARE @attributes_json NVARCHAR(MAX) = JSON_QUERY(@variants_json, @variant_path + '.attributes');

            -- Generate SKU
            DECLARE @sku VARCHAR(100) = @product_code;
            DECLARE @attr_names VARCHAR(MAX) = '';

            -- Build SKU suffix from attributes
            SELECT @sku = @sku + '-' + UPPER(av.value),
                   @attr_names = @attr_names + av.display_value + ' '
            FROM OPENJSON(@attributes_json)
            WITH (
                attribute_name VARCHAR(50) '$.name',
                attribute_value VARCHAR(100) '$.value'
            ) AS attr
            JOIN Attribute a ON a.name = attr.attribute_name
            JOIN AttributeValue av ON av.attribute_id = a.id AND av.value = attr.attribute_value
            ORDER BY a.sort_order;

            -- Check if SKU exists, append number if needed
            DECLARE @sku_exists INT = 1;
            DECLARE @sku_counter INT = 1;
            DECLARE @original_sku VARCHAR(100) = @sku;

            WHILE @sku_exists = 1
            BEGIN
                IF NOT EXISTS (SELECT 1 FROM ProductVariant WHERE sku = @sku)
                BEGIN
                    SET @sku_exists = 0;
                END
                ELSE
                BEGIN
                    SET @sku = @original_sku + '-' + CAST(@sku_counter AS VARCHAR);
                    SET @sku_counter = @sku_counter + 1;
                END
            END

            -- B3: Create Variant
            INSERT INTO ProductVariant (
                product_id, sku, price_adjustment, quantity, barcode
            )
            VALUES (
                @new_product_id, @sku, @price_adjustment, @quantity,
                @sku  -- Use SKU as default barcode
            );

            DECLARE @variant_id BIGINT = SCOPE_IDENTITY();

            -- B4: Map Attributes
            INSERT INTO VariantAttribute (variant_id, attribute_value_id)
            SELECT @variant_id, av.id
            FROM OPENJSON(@attributes_json)
            WITH (
                attribute_name VARCHAR(50) '$.name',
                attribute_value VARCHAR(100) '$.value'
            ) AS attr
            JOIN Attribute a ON a.name = attr.attribute_name
            JOIN AttributeValue av ON av.attribute_id = a.id AND av.value = attr.attribute_value;

            SET @variant_idx = @variant_idx + 1;
        END

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_GetProductWithVariants]    Script Date: 23/04/2026 1:05:19 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- SP: Get Product with Variants
CREATE   PROCEDURE [dbo].[sp_GetProductWithVariants]
    @product_id BIGINT
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Product info
    SELECT 
        p.id,
        p.product_code,
        p.name,
        p.description,
        p.base_price,
        p.discount_percent,
        p.image_url,
        p.category_id,
        c.name AS category_name,
        p.total_quantity,
        p.is_active
    FROM Product p
    LEFT JOIN Category c ON p.category_id = c.id
    WHERE p.id = @product_id;
    
    -- Variants with attributes
    SELECT 
        pv.id AS variant_id,
        pv.sku,
        pv.price_adjustment,
        pv.quantity,
        pv.barcode,
        pv.is_active,
        (
            SELECT 
                a.name AS attribute_name,
                a.display_name,
                av.value,
                av.display_value,
                av.hex_code
            FROM VariantAttribute va
            JOIN AttributeValue av ON va.attribute_value_id = av.id
            JOIN Attribute a ON av.attribute_id = a.id
            WHERE va.variant_id = pv.id
            FOR JSON PATH
        ) AS attributes
    FROM ProductVariant pv
    WHERE pv.product_id = @product_id
    ORDER BY pv.sku;
END;
GO
USE [master]
GO
ALTER DATABASE [ASM_Java5] SET  READ_WRITE 
GO
