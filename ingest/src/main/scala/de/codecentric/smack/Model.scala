package de.codecentric.smack

case class Artist(
                   name: String
                 )

case class Track(
                  album_name: String,
                  artists: List[Artist],
                  disc_number: Double,
                  duration_ms: Double,
                  explicit: Boolean,
                  id: String,
                  is_playable: Boolean,
                  name: String,
                  popularity: Double,
                  track_number: Double
                )