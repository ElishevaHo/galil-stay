package com.galilstay.server.dto;

/** יחידה פנויה בתוצאות חיפוש, יחד עם הצעת המחיר לתאריכים שנבחרו */
public record AvailableUnit(UnitDto unit, PriceQuote quote) {
}
