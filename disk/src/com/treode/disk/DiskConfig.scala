package com.treode.disk

class DiskConfig private [disk] (
    val cell: CellId,
    val superBlockBits: Int,
    val maximumRecordBytes: Int,
    val maximumPageBytes: Int,
    val checkpointBytes: Int,
    val checkpointEntries: Int,
    val cleaningFrequency: Int,
    val cleaningLoad: Int
) {

  val superBlockBytes = 1 << superBlockBits
  val superBlockMask = superBlockBytes - 1
  val diskLeadBytes = 1 << (superBlockBits + 1)

  val minimumSegmentBits = {
    val bytes = math.max (maximumRecordBytes, maximumPageBytes)
    Integer.SIZE - Integer.numberOfLeadingZeros (bytes - 1) + 1
  }

  def checkpoint (bytes: Int, entries: Int): Boolean =
    bytes > checkpointBytes || entries > checkpointEntries

  def clean (segments: Int): Boolean =
    segments >= cleaningFrequency
}

object DiskConfig {

  def apply (
      cell: CellId,
      superBlockBits: Int,
      maximumRecordBytes: Int,
      maximumPageBytes: Int,
      checkpointBytes: Int,
      checkpointEntries: Int,
      cleaningFrequency: Int,
      cleaningLoad: Int
  ): DiskConfig = {

    require (
        superBlockBits > 0,
        "A superblock must have more than 0 bytes.")
    require (
        maximumRecordBytes > 0,
        "The maximum record size must be more than 0 bytes.")
    require (
        maximumPageBytes > 0,
        "The maximum page size must be more than 0 bytes.")
    require (
        checkpointBytes > 0,
        "The checkpoint interval must be more than 0 bytes.")
    require (
        checkpointEntries > 0,
        "The checkpoint interval must be more than 0 entries.")
    require (
        cleaningFrequency > 0,
        "The cleaning interval must be more than 0 segments.")
    require (
        cleaningLoad > 0,
        "The cleaning load must be more than 0 segemnts.")

    new DiskConfig (
        cell,
        superBlockBits,
        maximumRecordBytes,
        maximumPageBytes,
        checkpointBytes,
        checkpointEntries,
        cleaningFrequency,
        cleaningLoad)
  }

  def recommended (
      cell: CellId,
      superBlockBits: Int = 14,
      maximumRecordBytes: Int = 1<<24,
      maximumPageBytes: Int = 1<<24,
      checkpointBytes: Int = 1<<24,
      checkpointEntries: Int = 10000,
      cleaningFrequency: Int = 7,
      cleaningLoad: Int = 1
  ): DiskConfig =
    DiskConfig (
        cell: CellId,
        superBlockBits,
        maximumRecordBytes,
        maximumPageBytes,
        checkpointBytes,
        checkpointEntries,
        cleaningFrequency,
        cleaningLoad)

  val pickler = {
    import DiskPicklers._
    wrap (cellId, uint, uint, uint, uint, uint, uint, uint)
    .build ((apply _).tupled)
    .inspect (v => (
        v.cell,
        v.superBlockBits,
        v.maximumRecordBytes,
        v.maximumPageBytes,
        v.checkpointBytes,
        v.checkpointEntries,
        v.cleaningFrequency,
        v.cleaningLoad))
  }}
